package com.origamilabs.orii.ui.main.alerts

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.appevents.AppEventsConstants
import com.origamilabs.orii.R
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.Application
import com.origamilabs.orii.ui.main.alerts.AlertItemAdapter
import kotlin.text.Regex

/**
 * Fragment affichant la liste des alertes, organisée en deux sections (contacts et applications).
 *
 * Ce fichier est le code source original, modernisé et débarrassé des éléments auto‑générés
 * (comme le _$_findViewCache et les méthodes associées) qui sont générés automatiquement lors de la compilation.
 */
class AlertsFragment : Fragment() {

    companion object {
        private const val TAG = "AlertsFragment"

        fun newInstance() = AlertsFragment()
    }

    private lateinit var alertItemAdapter: AlertItemAdapter
    private lateinit var alertsRecyclerView: RecyclerView
    private lateinit var alertsRecyclerViewManager: RecyclerView.LayoutManager
    private lateinit var viewModel: AlertsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.alerts_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Récupération du ViewModel (ici, on utilise la méthode moderne ViewModelProvider)
        viewModel = ViewModelProvider(this)[AlertsViewModel::class.java]

        // Obtention des listes de données depuis le ViewModel
        val peopleList = viewModel.peopleList
        val appsList = viewModel.appsList

        // Initialisation de l'adapter avec les données
        alertItemAdapter = AlertItemAdapter(this, peopleList, appsList)

        // Configuration de la RecyclerView
        alertsRecyclerViewManager = LinearLayoutManager(context)
        alertsRecyclerView = requireView().findViewById(R.id.alerts_recycler_view)
        alertsRecyclerView.setHasFixedSize(true)
        alertsRecyclerView.layoutManager = alertsRecyclerViewManager
        alertsRecyclerView.adapter = alertItemAdapter

        // Configuration du glissement (swipe) sur les éléments de la section contacts
        alertItemAdapter.setItemTouchHelper(alertsRecyclerView)
    }

    /**
     * Lance l'intention de sélection de contact dans le gestionnaire de contacts.
     */
    fun pickContact() {
        startActivityForResult(
            Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
            2
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2 && data != null && resultCode == Activity.RESULT_OK) {
            val dataUri: Uri = data.data ?: throw NullPointerException("data.data is null")
            val activity = activity ?: throw NullPointerException("activity is null")
            val contentResolver: ContentResolver = activity.contentResolver

            val query: Cursor = contentResolver.query(dataUri, null, null, null, null)
                ?: throw NullPointerException("Query returned null")
            query.moveToFirst()

            val hasPhoneNumber = query.getString(query.getColumnIndexOrThrow("has_phone_number"))
            val contactId = query.getString(query.getColumnIndexOrThrow("_id"))

            if (hasPhoneNumber == AppEventsConstants.EVENT_PARAM_VALUE_YES) {
                val query2: Cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    "contact_id = $contactId",
                    null,
                    null
                ) ?: throw NullPointerException("Query2 returned null")

                var phoneRecordId = ""
                var displayName = ""

                // Parcours des numéros de téléphone associés
                while (query2.moveToNext()) {
                    phoneRecordId = query2.getString(query2.getColumnIndex("_id"))
                    displayName = query2.getString(query2.getColumnIndex("display_name"))
                    val phoneNumber = query2.getString(query2.getColumnIndex("data1"))
                    // Nettoyage du numéro de téléphone en supprimant les caractères spéciaux
                    val cleanedNumber = Regex("[-() ]").replace(phoneNumber, "")
                    // La variable 'cleanedNumber' n'est pas utilisée dans cet exemple, mais pourrait l'être si besoin.
                }
                query2.close()

                // Vérification si ce contact n'est pas déjà présent
                val isExisting = AppManager.availablePeople.any { it.pid == phoneRecordId.toInt() }
                if (!isExisting) {
                    // Création d'un nouvel objet Application (utilisé ici pour représenter un contact)
                    val application = Application(phoneRecordId.toInt(), 0, 0, displayName)
                    viewModel.addPerson(application)
                    alertItemAdapter.addPeopleItem(application)
                    AnalyticsManager.logContactCount()
                }
            } else {
                Toast.makeText(activity, R.string.contact_no_phone_number, Toast.LENGTH_SHORT).show()
            }
            query.close()
        }
    }
}
