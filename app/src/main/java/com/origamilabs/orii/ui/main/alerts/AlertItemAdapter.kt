package com.origamilabs.orii.ui.main.alerts

import android.content.Intent
import android.graphics.Canvas
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.origamilabs.orii.R
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.Alert
import com.origamilabs.orii.models.Application  // Modèle pour les apps
import com.origamilabs.orii.models.Person       // Modèle pour les contacts (people)
 
/**
 * Adapter pour afficher des éléments d’alerte dans une RecyclerView.
 *
 * La liste affichée comporte deux sections : une pour les contacts (people) et une pour les applications (apps).
 * Des en-têtes (header) sont insérés avant chaque section.
 *
 * Ce fichier correspond au code source « humain » original, épuré des éléments auto-générés par le compilateur.
 */
class AlertItemAdapter(
    private val fragment: AlertsFragment,
    private val peopleData: ArrayList<AlertItem>,
    private val appsData: ArrayList<AlertItem>
) : RecyclerView.Adapter<AlertItemAdapter.ViewHolder>() {

    private var mRecyclerView: RecyclerView? = null

    companion object {
        private const val TAG = "AlertItemAdapter"
    }

    inner class ViewHolder(val item: View) : RecyclerView.ViewHolder(item)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun getItemViewType(position: Int): Int = when {
        position == 0 -> AlertItem.ItemType.PEOPLE_HEADER.ordinal
        position in 1..peopleData.size -> AlertItem.ItemType.PEOPLE.ordinal
        position == peopleData.size + 1 -> AlertItem.ItemType.APPS_HEADER.ordinal
        else -> AlertItem.ItemType.APPS.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item: View = when (viewType) {
            AlertItem.ItemType.PEOPLE_HEADER.ordinal ->
                LayoutInflater.from(parent.context).inflate(R.layout.alerts_people_header, parent, false)
            AlertItem.ItemType.APPS_HEADER.ordinal ->
                LayoutInflater.from(parent.context).inflate(R.layout.alerts_apps_header, parent, false)
            else ->
                LayoutInflater.from(parent.context).inflate(R.layout.alert_item, parent, false)
        }
        return ViewHolder(item)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            AlertItem.ItemType.PEOPLE_HEADER.ordinal -> {
                // Configuration de l'en-tête pour la section contacts.
                holder.item.findViewById<ImageView>(R.id.add_person_button)?.setOnClickListener {
                    fragment.pickContact()
                }
                val emptyLayout = holder.item.findViewById<ConstraintLayout>(R.id.empty_layout)
                emptyLayout?.visibility = if (peopleData.isEmpty()) View.VISIBLE else View.GONE
            }
            AlertItem.ItemType.APPS_HEADER.ordinal -> {
                // Configuration de l'en-tête pour la section apps.
                holder.item.findViewById<ImageView>(R.id.alert_info_button)?.setOnClickListener {
                    val intent = Intent(fragment.context, AlertInfoActivity::class.java)
                    fragment.startActivity(intent)
                    AnalyticsManager.logAlertInfoPage()
                }
            }
            AlertItem.ItemType.PEOPLE.ordinal, AlertItem.ItemType.APPS.ordinal -> {
                // Récupération de l’élément d’alerte correspondant
                val alertItem = when (getItemViewType(position)) {
                    AlertItem.ItemType.PEOPLE.ordinal -> peopleData[position - 1]
                    AlertItem.ItemType.APPS.ordinal -> appsData[position - (peopleData.size + 2)]
                    else -> return
                }

                // Mise à jour de l’affichage en fonction du type de données
                val data = alertItem.data
                when (data) {
                    is Person -> {
                        updateView(holder, data.personName, data.ledColor, data.vibration)
                    }
                    is Application -> {
                        updateView(holder, data.appName, data.ledColor, data.vibration)
                    }
                }
                Log.d(TAG, "expanded: ${alertItem.expanded}")

                // Activation/désactivation du détail
                holder.item.findViewById<Button>(R.id.detail_activator)?.setOnClickListener {
                    alertItem.expanded = !alertItem.expanded
                    if (alertItem.expanded) {
                        (mRecyclerView?.layoutManager as? LinearLayoutManager)
                            ?.scrollToPositionWithOffset(position, 30)
                    }
                    notifyItemChanged(position)
                }
                val detailLayout = holder.item.findViewById<ConstraintLayout>(R.id.detail_layout)
                detailLayout?.visibility = if (alertItem.expanded) View.VISIBLE else View.GONE

                val detailActivatorImage = holder.item.findViewById<ImageView>(R.id.detail_activator_image)
                detailActivatorImage?.setImageResource(
                    if (alertItem.expanded)
                        R.drawable.ic_keyboard_arrow_down_gray_24dp
                    else
                        R.drawable.ic_keyboard_arrow_right_gray_24dp
                )

                // Modification de la couleur LED via les flèches haut/bas
                holder.item.findViewById<ImageView>(R.id.colorDropUpArrow)?.setOnClickListener {
                    val alert = data as? Alert
                        ?: throw TypeCastException("null cannot be cast to non-null type com.origamilabs.orii.models.Alert")
                    alert.changeLedColor(alert.ledColor + 1)
                    updateData(alert)
                    notifyItemChanged(position)
                }
                holder.item.findViewById<ImageView>(R.id.colorDropDownArrow)?.setOnClickListener {
                    val alert = data as? Alert
                        ?: throw TypeCastException("null cannot be cast to non-null type com.origamilabs.orii.models.Alert")
                    alert.changeLedColor(alert.ledColor - 1)
                    updateData(alert)
                    notifyItemChanged(position)
                }

                // Modification de la vibration via les flèches haut/bas
                holder.item.findViewById<ImageView>(R.id.vibrationDropUpArrow)?.setOnClickListener {
                    val alert = data as? Alert
                        ?: throw TypeCastException("null cannot be cast to non-null type com.origamilabs.orii.models.Alert")
                    alert.changeVibration(alert.vibration + 1)
                    updateData(alert)
                    notifyItemChanged(position)
                }
                holder.item.findViewById<ImageView>(R.id.vibrationDropDownArrow)?.setOnClickListener {
                    val alert = data as? Alert
                        ?: throw TypeCastException("null cannot be cast to non-null type com.origamilabs.orii.models.Alert")
                    alert.changeVibration(alert.vibration - 1)
                    updateData(alert)
                    notifyItemChanged(position)
                }

                // Désactivation de l’activation du détail pour certaines applications
                if (data is Application && data.packageName == "phonecall") {
                    holder.item.findViewById<Button>(R.id.detail_activator)?.isClickable = false
                    holder.item.findViewById<ImageView>(R.id.detail_activator_image)?.visibility = View.INVISIBLE
                }
                holder.item.findViewById<ConstraintLayout>(R.id.foreground_layout)?.translationX = 0f
            }
        }
    }

    /**
     * Met à jour les données d’un élément d’alerte dans la base (en arrière-plan)
     * et envoie les statistiques associées via AnalyticsManager.
     */
    fun updateData(itemData: Alert) {
        when (itemData) {
            is Application -> {
                AppManager.runQueryOnBackground {
                    AppManager.database.applicationDao().update(itemData)
                    AnalyticsManager.logAppAlertConf(itemData.appName, itemData.vibration, itemData.ledColor)
                }
            }
            is Person -> {
                AppManager.runQueryOnBackground {
                    AppManager.database.personDao().update(itemData)
                    AnalyticsManager.logContactAlertConf(itemData.pid, itemData.vibration, itemData.ledColor)
                }
            }
        }
    }

    /**
     * Met à jour la vue d’un élément en affichant le contenu, la couleur LED et la vibration.
     */
    private fun updateView(holder: ViewHolder, content: String, ledColor: Int, vibration: Int) {
        holder.item.findViewById<TextView>(R.id.content_text)?.text = content
        holder.item.findViewById<ImageView>(R.id.color_image)?.setImageLevel(ledColor)
        holder.item.findViewById<ImageView>(R.id.selected_color)?.setImageLevel(ledColor)
        holder.item.findViewById<ImageView>(R.id.vibration_image)?.setImageLevel(vibration)
        holder.item.findViewById<ImageView>(R.id.selected_vibration)?.setImageLevel(vibration)
    }

    /**
     * Ajoute un contact à la section people.
     */
    fun addPeopleItem(person: Person) {
        peopleData.add(AlertItem(person))
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = peopleData.size + appsData.size + 2

    /**
     * Configure un ItemTouchHelper pour autoriser, dans la section people, le glissement (swipe)
     * permettant de supprimer un contact.
     */
    fun setItemTouchHelper(alertsRecyclerView: RecyclerView) {
        val callback = object : ItemTouchHelper.Callback() {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val adapterPosition = viewHolder.adapterPosition
                return if (adapterPosition in 1..peopleData.size) {
                    ItemTouchHelper.Callback.makeMovementFlags(0, ItemTouchHelper.END)
                } else {
                    ItemTouchHelper.Callback.makeMovementFlags(0, 0)
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapterPosition = viewHolder.adapterPosition
                val alertItem = peopleData[adapterPosition - 1]
                Log.d(TAG, "Removed 1 contact, position=$adapterPosition")
                peopleData.remove(alertItem)
                val person = alertItem.data as? Person
                    ?: throw TypeCastException("null cannot be cast to non-null type com.origamilabs.orii.models.Person")
                notifyItemRemoved(adapterPosition)
                if (peopleData.isEmpty()) {
                    notifyItemChanged(0)
                }
                AppManager.runQueryOnBackground {
                    AppManager.database.personDao().delete(person)
                    AppManager.availablePeople.remove(person)
                    AnalyticsManager.logContactCount()
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val foreground = viewHolder.itemView.findViewById<ConstraintLayout>(R.id.foreground_layout)
                ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                    c,
                    recyclerView,
                    foreground,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(alertsRecyclerView)
    }
}
