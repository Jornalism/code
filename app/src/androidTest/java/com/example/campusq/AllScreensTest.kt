package com.example.campusq

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.campusq.ui.showcase.CampusQPreviewContent
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class AllScreensTest(private val screen: Int, private val title: String) {
    @get:Rule val compose = createComposeRule()

    @Test fun phoneScreenRendersAndCaptures() = render(false)
    @Test fun compactLargeTextScreenRendersAndCaptures() = render(true)

    private fun render(compact: Boolean) {
        compose.setContent {
            if (compact) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                    Box(Modifier.requiredSize(320.dp, 640.dp)) { CampusQPreviewContent(screen) }
                }
            } else CampusQPreviewContent(screen)
        }
        compose.waitForIdle()
        assertTrue("Missing screen content: $title", compose.onAllNodes(hasText(title, substring = true)).fetchSemanticsNodes().isNotEmpty())
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = requireNotNull(context.getExternalFilesDir("phase2"))
        directory.mkdirs()
        val file = File(directory, "%02d-%s.png".format(screen, if (compact) "compact-large-text" else "phone"))
        file.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        assertTrue(file.length() > 0)
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        android.os.ParcelFileDescriptor.AutoCloseInputStream(
            automation.executeShellCommand("mkdir -p /sdcard/Download/CampusQPhase2")
        ).use { it.readBytes() }
        android.os.ParcelFileDescriptor.AutoCloseInputStream(
            automation.executeShellCommand("cp ${file.absolutePath} /sdcard/Download/CampusQPhase2/${file.name}")
        ).use { it.readBytes() }
    }

    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}: {1}")
        fun screens(): List<Array<Any>> = listOf(
            arrayOf(1, "Campus"), arrayOf(2, "Welcome Back!"), arrayOf(3, "Create Account"),
            arrayOf(4, "Quick Actions"), arrayOf(5, "Registrar Services"), arrayOf(6, "Queue Status"),
            arrayOf(7, "Book Appointment"), arrayOf(8, "Transaction History"), arrayOf(9, "Profile"),
            arrayOf(10, "Registrar Dashboard"), arrayOf(11, "Queue Management"), arrayOf(12, "Ticket Details"),
            arrayOf(13, "Service in Progress"), arrayOf(14, "Appointments"), arrayOf(15, "Service Status"),
            arrayOf(16, "Profile"), arrayOf(17, "Admin Dashboard"), arrayOf(18, "Manage Services"),
            arrayOf(19, "Staff Management"), arrayOf(20, "Queue Monitoring"), arrayOf(21, "Appointments"),
            arrayOf(22, "Reports"), arrayOf(23, "System Settings"), arrayOf(24, "CampusQ UI Preview")
        )
    }
}
