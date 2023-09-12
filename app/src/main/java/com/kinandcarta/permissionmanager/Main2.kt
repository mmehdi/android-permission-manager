package com.kinandcarta.permissionmanager

import android.app.ActivityManager
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.annotation.ColorInt

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kinandcarta.permissionmanager.databinding.MainFragmentBinding
import com.kinandcarta.permissionmanager.permissions.Permission
import com.kinandcarta.permissionmanager.permissions.PermissionManagerActivity


class Main2 : AppCompatActivity() {
    private val binding by lazy { MainFragmentBinding.inflate(layoutInflater) }
    private val permissionManager = PermissionManagerActivity.from(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = binding.root
        setContentView(view)

        binding.camera.setOnClickListener {

            permissionManager.request(Permission.Camera)
                .rationale("We need permission to see your beautiful face")
                .checkPermission { granted ->
                    if (granted) {
                        success("We can see your face :)")
                    } else {
                        error("We couldn't access the camera :(")
                    }
                }

        }

        binding.bundled.setOnClickListener {
            permissionManager
                // Check a few bundled permissions under one: Storage = Read + Write
                .request(Permission.MandatoryForFeatureOne)
                .rationale("We require to demonstrate that we can request two permissions at once")
                .checkPermission { granted ->
                    if (granted) {
                        success("YES! Now I can access Storage and Location!")
                    } else {
                        error("Still missing at least one permission :(")
                    }
                }
        }

        binding.everything.setOnClickListener {
            permissionManager
                // Check all permissions without bundling them
                .request(Permission.Storage, Permission.Location, Permission.Camera)
                .rationale("We want permission for Storage (Read+Write), Location (Fine+Coarse) and Camera")
                .checkDetailedPermission { result ->
                    if (result.all { it.value }) {
                        success("YES! Now I have full access :D")
                    } else {
                        showErrorDialog(result)
                    }
                }
        }

        binding.clear.setOnClickListener {
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            manager.clearApplicationUserData()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager?.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }

    private fun success(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .withColor(Color.parseColor("#09AF00"))
            .show()
    }

    private fun error(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .withColor(Color.parseColor("#B00020"))
            .show()
    }
    private fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar {
        this.view.setBackgroundColor(colorInt)
        return this
    }
    private fun getErrorMessageFor(permission: Permission) = when (permission) {
        Permission.Camera -> "Camera permission: "
        Permission.Location -> "Location permission"
        Permission.Storage -> "Storage permission"
        else -> "Unknown permission"
    }
    private fun showErrorDialog(result: Map<Permission, Boolean>) {
        val message = result.entries.fold("") { message, entry ->
            message + "${getErrorMessageFor(entry.key)}: ${entry.value}\n"
        }
        Log.i("TAG", message)
        AlertDialog.Builder(applicationContext)
            .setTitle("Missing permissions")
            .setMessage(message)
            .show()
    }
}