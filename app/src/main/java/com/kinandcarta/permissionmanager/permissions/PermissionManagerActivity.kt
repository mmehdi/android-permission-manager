package com.kinandcarta.permissionmanager.permissions
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kinandcarta.permissionmanager.R


class PermissionManagerActivity(private val activity: Activity) {

    private val requiredPermissions = mutableListOf<Permission>()
    private var rationale: String? = null
    private var callback: (Boolean) -> Unit = {}
    private var detailedCallback: (Map<Permission,Boolean>) -> Unit = {}



    companion object {
        fun from(activity: Activity) =
            PermissionManagerActivity(activity)
    }




    fun onRequestPermissionsResult( requestCode: Int,
                                    permissions: Array<out String>,
                                    grantResults: IntArray){

        val map = mutableMapOf<String,Boolean>()
        var i =0
       permissions.forEach {
           map.put(it,if(grantResults[i]==0) true else false)
           i++
       }

        sendResultAndCleanUp(map)
    }



    private fun requestPermissions() {
        ActivityCompat.requestPermissions(activity, getPermissionList(),1)
    }

    fun request(vararg permission: Permission): PermissionManagerActivity {
        requiredPermissions.addAll(permission)
        return this
    }
    fun checkPermission(callback: (Boolean) -> Unit) {
        this.callback = callback
        handlePermissionRequest()
    }
    fun rationale(description: String): PermissionManagerActivity {
        rationale = description
        return this
    }

    fun checkDetailedPermission(callback: (Map<Permission,Boolean>) -> Unit) {
        this.detailedCallback = callback
        handlePermissionRequest()
    }

    private fun handlePermissionRequest() {
        activity.let { activity ->
            when {
                areAllPermissionsGranted(activity) -> sendPositiveResult()
                shouldShowPermissionRationale(activity) -> displayRationale(activity)
                else -> requestPermissions()
            }
        }
    }


    private fun displayRationale(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.dialog_permission_title))
            .setMessage(rationale ?: activity.getString(R.string.dialog_permission_default_message))
            .setCancelable(false)
            .setPositiveButton(activity.getString(R.string.dialog_permission_button_positive)) { _, _ ->
                requestPermissions()
            }
            .show()
    }

    /*private fun requestPermissions() {
        permissionCheck?.launch(getPermissionList())
    }*/

    private fun cleanUp() {
        requiredPermissions.clear()
        rationale = null
        callback = {}
        detailedCallback = {}
    }


    private fun sendPositiveResult() {
        sendResultAndCleanUp(getPermissionList().associate { it to true } )
    }

    private fun sendResultAndCleanUp(grantResults: Map<String, Boolean>) {
        callback(grantResults.all { it.value })
        detailedCallback(grantResults.mapKeys { Permission.from(it.key) })
        cleanUp()
    }


    private fun areAllPermissionsGranted(activity: Activity) =
        requiredPermissions.all { it.isGranted(activity) }

    private fun shouldShowPermissionRationale(activity: Activity) =
        requiredPermissions.any { it.requiresRationale(activity) }

    private fun getPermissionList() =
        requiredPermissions.flatMap { it.permissions.toList() }.toTypedArray()

    private fun Permission.isGranted(activity: Activity) =
        permissions.all { hasPermission(activity, it) }

    private fun Permission.requiresRationale(activity: Activity) =
        permissions.any { activity.shouldShowRequestPermissionRationale(it) }

    private fun hasPermission(activity: Activity, permission: String) =
        ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
}