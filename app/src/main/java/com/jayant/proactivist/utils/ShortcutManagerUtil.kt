package com.jayant.proactivist.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.ChatListActivity
import com.jayant.proactivist.activities.HelpActivity
import com.jayant.proactivist.activities.ProfileActivity
import com.jayant.proactivist.activities.ViewApplicationsActivity
import java.util.*

object ShortcutManagerUtil {
    fun removeShortcuts(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager: ShortcutManager = context.getSystemService(ShortcutManager::class.java)
            shortcutManager.disableShortcuts(listOf("profile", "chat", "application", "help"))
            shortcutManager.removeAllDynamicShortcuts()
        }
    }

    fun createShortcut(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val sm = context.getSystemService(ShortcutManager::class.java)
            val intent1 = Intent(context, ProfileActivity::class.java)
            intent1.action = Intent.ACTION_VIEW
            val shortcut1 = ShortcutInfo.Builder(context, "profile")
                .setIntent(intent1)
                .setLongLabel("My Profile")
                .setShortLabel("Click to open my profile")
                .setDisabledMessage("Login to open this")
                .setIcon(Icon.createWithResource(context, R.drawable.ic_profile))
                .build()

            val intent2 = Intent(context, ChatListActivity::class.java)
            intent2.action = Intent.ACTION_VIEW
            val shortcut2 = ShortcutInfo.Builder(context, "chat")
                .setIntent(intent2)
                .setLongLabel("My Chats")
                .setShortLabel("Click to open chats")
                .setDisabledMessage("Login to open this")
                .setIcon(Icon.createWithResource(context, R.drawable.ic_message_black))
                .build()

            val intent3 = Intent(context, ViewApplicationsActivity::class.java)
            intent3.action = Intent.ACTION_VIEW
            val shortcut3 = ShortcutInfo.Builder(context, "application")
                .setIntent(intent3)
                .setLongLabel("Applications")
                .setShortLabel("Click to view applications")
                .setDisabledMessage("Login to open this")
                .setIcon(Icon.createWithResource(context, R.drawable.ic_resume_black))
                .build()

            val intent4 = Intent(context, HelpActivity::class.java)
            intent4.action = Intent.ACTION_VIEW
            val shortcut4 = ShortcutInfo.Builder(context, "help")
                .setIntent(intent3)
                .setLongLabel("Help")
                .setShortLabel("Click for help")
                .setDisabledMessage("Login to open this")
                .setIcon(Icon.createWithResource(context, R.drawable.ic_help))
                .build()

            sm.dynamicShortcuts = listOf(shortcut1, shortcut2, shortcut3, shortcut4)
        }
    }
}