package com.origamilabs.orii.manager

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.ContactsContract
import android.util.Log
import androidx.room.Room
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.origamilabs.orii.Constants
import com.origamilabs.orii.api.API
import com.origamilabs.orii.db.AppDatabase
import com.origamilabs.orii.db.SharedPreferences
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.models.Application
import com.origamilabs.orii.models.FirmwareVersionInfo
import com.origamilabs.orii.models.User
import com.origamilabs.orii.models.VoiceAssistantCounter
import io.fabric.sdk.android.services.common.AbstractSpiCall
import java.io.File
import java.util.*

/**
 * Singleton qui gère l'état global de l'application, y compris la base de données,
 * les préférences partagées, l'utilisateur courant, et d'autres informations système.
 */
object AppManager {
    private const val TAG = "AppManager"

    private var applicationContext: Context? = null
    private var canFirmwareForceUpdate: Boolean = false
    private var canFirmwareUpdate: Boolean = false
    private var currentUser: User? = null
    private var firmwareVersionChecked: Boolean = false
    private var firmwareVersionInfo: FirmwareVersionInfo? = null

    lateinit var database: AppDatabase
        private set
    lateinit var dbQueryHandler: Handler
        private set
    lateinit var dbQueryThread: HandlerThread
        private set
    lateinit var sharedPreferences: SharedPreferences
        private set

    private var availableApps: ArrayList<Application> = ArrayList()
    // Note : Ici, on utilise le même type "Application" pour représenter une personne.
    // Adaptez ce type si votre modèle de "personne" est différent.
    private var availablePeople: ArrayList<Application> = ArrayList()

    private var batteryLevel: Int = -1
    private var firmwareVersion: Int = -1

    fun close() {
        // Méthode vide (à compléter selon vos besoins)
    }

    fun start() {
        // Méthode vide (à compléter selon vos besoins)
    }

    fun getDatabase(): AppDatabase = database

    fun setDatabase(appDatabase: AppDatabase) {
        database = appDatabase
    }

    fun getAvailableApps(): ArrayList<Application> = availableApps

    fun setAvailableApps(apps: ArrayList<Application>) {
        availableApps = apps
    }

    fun getAvailablePeople(): ArrayList<Application> = availablePeople

    fun setAvailablePeople(people: ArrayList<Application>) {
        availablePeople = people
    }

    fun getDbQueryHandler(): Handler = dbQueryHandler

    fun setDbQueryHandler(handler: Handler) {
        dbQueryHandler = handler
    }

    fun getDbQueryThread(): HandlerThread = dbQueryThread

    fun setDbQueryThread(handlerThread: HandlerThread) {
        dbQueryThread = handlerThread
    }

    fun getSharedPreferences(): SharedPreferences = sharedPreferences

    fun setSharedPreferences(sp: SharedPreferences) {
        sharedPreferences = sp
    }

    fun getCurrentUser(): User? = currentUser

    fun setCurrentUser(user: User) {
        currentUser = user
    }

    fun getBatteryLevel(): Int = batteryLevel

    fun setBatteryLevel(level: Int) {
        batteryLevel = level
    }

    fun getFirmwareVersion(): Int = firmwareVersion

    fun setFirmwareVersion(version: Int) {
        firmwareVersion = version
    }

    fun getFirmwareVersionChecked(): Boolean = firmwareVersionChecked

    fun setFirmwareVersionChecked(checked: Boolean) {
        firmwareVersionChecked = checked
    }

    fun getCanFirmwareUpdate(): Boolean = canFirmwareUpdate

    fun setCanFirmwareUpdate(update: Boolean) {
        canFirmwareUpdate = update
    }

    fun getCanFirmwareForceUpdate(): Boolean = canFirmwareForceUpdate

    fun setCanFirmwareForceUpdate(forceUpdate: Boolean) {
        canFirmwareForceUpdate = forceUpdate
    }

    fun getFirmwareVersionInfo(): FirmwareVersionInfo? = firmwareVersionInfo

    fun setFirmwareVersionInfo(info: FirmwareVersionInfo) {
        firmwareVersionInfo = info
    }

    /**
     * Initialise l'AppManager avec le contexte de l'application.
     * Configure la base de données Room, les préférences partagées, les threads pour les requêtes DB,
     * et effectue diverses initialisations (apps installées, utilisateur, FCM, etc.).
     */
    fun init(applicationContext: Context) {
        this.applicationContext = applicationContext

        // Création de la base de données via Room.
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "orii-app-db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

        sharedPreferences = SharedPreferences.INSTANCE.apply { init(applicationContext) }

        // Initialisation d'un thread dédié aux requêtes en base de données.
        dbQueryThread = HandlerThread("DatabaseThread").apply { start() }
        dbQueryHandler = Handler(dbQueryThread.looper)

        initAvailableApps()
        initAvailablePeople()
        initUser()
        initVoiceAssistantCounterTimer()
        initFcm()
    }

    private fun initFcm() {
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                Log.d(TAG, if (!task.isSuccessful) "Subscribe failed" else "Subscribed <all> topic")
            }

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            Log.d(TAG, instanceIdResult.token)
        }
    }

    private fun initVoiceAssistantCounterTimer() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val allCounters: List<VoiceAssistantCounter> = getDatabase().vaCounterDao().getAll()
                if (currentUser == null || allCounters.isEmpty()) return

                val jsonArray = Gson().toJsonTree(
                    allCounters,
                    object : TypeToken<ArrayList<VoiceAssistantCounter>>() {}.type
                ) as? JsonArray ?: throw TypeCastException("null cannot be cast to non-null type JsonArray")

                API.postViceAssistantCounter(
                    currentUser!!.id,
                    currentUser!!.token,
                    getUuid(),
                    jsonArray,
                    object : API.ResponseListener {
                        override fun onSuccess(response: JsonObject) {
                            AnalyticsManager.logVaTriggered(jsonArray)
                            runQueryOnBackground {
                                getDatabase().vaCounterDao().clear()
                            }
                        }

                        override fun onError(errorMessage: String) {
                            // Traiter l'erreur (ex. token expiré)
                        }
                    }
                )
            }
        }, 0L, 7_200_000L) // Exécuté toutes les 2 heures (7 200 000 ms)
    }

    private fun initUser() {
        currentUser = sharedPreferences.getUser()
    }

    /**
     * Exécute une requête en arrière-plan via le handler dédié.
     */
    fun runQueryOnBackground(runnable: Runnable) {
        dbQueryHandler.post(runnable)
    }

    /**
     * Initialise la liste des applications disponibles en vérifiant si elles sont installées.
     * Pour chaque app supportée, une copie est insérée ou mise à jour dans la base de données.
     */
    private fun initAvailableApps() {
        for (app in Constants.SUPPORTED_APPS) {
            if (isAppInstalled(app.packageName)) {
                Log.d(TAG, "${app.packageName} is installed")
                // Créez une copie de l'objet avec des paramètres par défaut.
                val appCopy = app.copy(
                    ledColor = 0,
                    vibration = 0,
                    appName = ""
                    // Ajoutez d'autres paramètres par défaut si nécessaire.
                )
                runQueryOnBackground {
                    val found = getDatabase().applicationDao().findByPackageName(app.packageName)
                    Log.d(TAG, "Found App: $found")
                    if (found != null) {
                        app.ledColor = found.ledColor
                        app.vibration = found.vibration
                        getDatabase().applicationDao().update(app)
                    } else {
                        getDatabase().applicationDao().insert(app)
                    }
                    availableApps.add(app)
                }
            }
        }
    }

    /**
     * Vérifie si une application est installée sur l'appareil.
     * Les packages "sms" et "phonecall" sont considérés comme toujours installés.
     */
    private fun isAppInstalled(packageName: String): Boolean {
        if (packageName == "sms" || packageName == "phonecall") return true
        return try {
            applicationContext?.packageManager?.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Initialise la liste des personnes disponibles à partir de la base de données.
     * Pour chaque personne, le nom de contact est mis à jour si disponible.
     */
    private fun initAvailablePeople() {
        runQueryOnBackground {
            val people = getDatabase().personDao().getAll()
            if (people.isEmpty()) throw UninitializedPropertyAccessException("peopleFromDb")
            for (person in people) {
                Log.d(TAG, "Person from db: $person")
                val name = getPersonNameById(person.pid)
                if (name != null) {
                    person.personName = name
                    // Créez une copie si nécessaire (si la classe est une data class).
                    val personCopy = person.copy(
                        ledColor = 0,
                        vibration = 0
                        // Ajoutez d'autres paramètres par défaut si nécessaire.
                    )
                    Log.d(TAG, "Person: $personCopy")
                    availablePeople.add(personCopy)
                    getDatabase().personDao().update(personCopy)
                }
            }
        }
    }

    /**
     * Récupère le nom du contact associé à l'ID fourni.
     */
    fun getPersonNameById(pid: Int): String? {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val ctx = applicationContext ?: throw UninitializedPropertyAccessException("applicationContext")
        val cursor: Cursor? = ctx.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            "_id = ?",
            arrayOf(pid.toString()),
            null
        )
        var name: String? = null
        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(0)
                Log.d(TAG, "Person (pid=$pid): $name")
            }
        }
        return name
    }

    /**
     * Récupère le nom du contact associé au numéro fourni.
     * Si aucun nom n'est trouvé, le numéro est retourné.
     */
    fun getPersonNameByNumber(number: String): String {
        var result = number
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        val ctx = applicationContext ?: throw UninitializedPropertyAccessException("applicationContext")
        ctx.contentResolver.query(uri, projection, null, null, null)?.use {
            if (it.moveToFirst()) {
                result = it.getString(0)
            }
        }
        return result
    }

    /**
     * Met à jour l'utilisateur courant lors de la connexion et le sauvegarde dans les SharedPreferences.
     */
    fun onUserLoggedIn(user: User) {
        currentUser = user
        sharedPreferences.setUser(user)
        Log.d(TAG, "Logged User: $user")
    }

    /**
     * Retourne true si un utilisateur est connecté.
     */
    fun isLogged(): Boolean = currentUser != null

    /**
     * Génère un UUID, le sauvegarde dans les SharedPreferences, puis le retourne.
     */
    private fun generateUuid(): String {
        val uuid = UUID.randomUUID().toString()
        Log.d(TAG, "Generated UUID: $uuid")
        sharedPreferences.setUuid(uuid)
        return uuid
    }

    /**
     * Retourne l'UUID stocké dans les SharedPreferences, ou en génère un nouveau si nécessaire.
     */
    fun getUuid(): String {
        val uuid = sharedPreferences.getUuid()
        return uuid ?: generateUuid()
    }
}
