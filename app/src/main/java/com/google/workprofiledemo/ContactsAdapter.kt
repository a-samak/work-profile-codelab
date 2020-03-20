package com.google.workprofiledemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Contact(val name: String, val isWork: Boolean)

class ContactsAdapter(private val contactList: MutableList<Contact>) :
    RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ContactViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.contact_item_layout, viewGroup, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.contact?.let {
            it.text = contact.name
            it.setCompoundDrawablesRelativeWithIntrinsicBounds(
                it.resources?.getDrawable(
                    when (contact.isWork) {
                        true -> R.drawable.ic_person_primary_40dp
                        false -> R.drawable.ic_person_green_40dp
                    }, null
                ), null, null, null
            )
        }
    }

    override fun getItemCount(): Int = contactList.size

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var contact: TextView? = itemView.findViewById(R.id.contact_name_tv) as TextView
    }
}