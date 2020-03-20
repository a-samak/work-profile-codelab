package com.google.workprofiledemo

import android.Manifest
import android.content.pm.CrossProfileApps
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.widget.AppCompatButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


const val PERMISSIONS_REQUEST_READ_CONTACTS = 1
const val PERSONAL_CONTACTS_LOADER_ID = 0
const val WORK_CONTACTS_LOADER_ID = 1

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var recyclerView: RecyclerView
    private lateinit var button: AppCompatButton
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var contacts: MutableList<Contact> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ContactsAdapter(contacts)
        recyclerView = findViewById<RecyclerView>(R.id.contacts_rv).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        val permission = checkSelfPermission(Manifest.permission.READ_CONTACTS)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS
            )
        } else {
            initLoaders()
        }

        val crossProfileApps = getSystemService(CrossProfileApps::class.java)
        val userHandles = crossProfileApps.targetUserProfiles
        val label = crossProfileApps.getProfileSwitchingLabel(userHandles[0])
        button = findViewById<AppCompatButton>(R.id.button).apply {
            text = label
            setOnClickListener {
                crossProfileApps.startMainActivity(
                    componentName,
                    userHandles.first()
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLoaders()
            } else {
                Toast.makeText(
                    this,
                    "Permission must be granted in order to display contacts information",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun initLoaders() {
        LoaderManager.getInstance(this).initLoader(PERSONAL_CONTACTS_LOADER_ID, null, this)
        LoaderManager.getInstance(this).initLoader(WORK_CONTACTS_LOADER_ID, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val nameFilter = Uri.encode("a") // names that start with W
        val contentURI = when (id) {
            PERSONAL_CONTACTS_LOADER_ID -> ContactsContract.Contacts.CONTENT_URI
            else -> {
                ContactsContract.Contacts.ENTERPRISE_CONTENT_FILTER_URI
                    .buildUpon()
                    .appendPath(nameFilter)
                    .appendQueryParameter(
                        ContactsContract.DIRECTORY_PARAM_KEY,
                        ContactsContract.Directory.ENTERPRISE_DEFAULT.toString()
                    )
                    .build()
            }
        }
        return CursorLoader(
            this, contentURI, arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            ), null, null, null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (data != null && data.count > 0) {
            while (data.moveToNext()) {
                val name = data.getString(0) ?: ""
                println(name)
                val contact = Contact(name, loader.id == 1)
                contacts.add(contact)
            }
        }
        viewAdapter.notifyDataSetChanged()
        data?.close()
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
    }
}
