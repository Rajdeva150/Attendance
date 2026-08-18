package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DutyCalculations
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DutyLog", appName)
  }

  @Test
  fun `test duty hours calculation standard and overnight`() {
    val standard = DutyCalculations.calculateDutyHours("09:00", "18:00")
    assertEquals(9.0, standard, 0.01)

    val overnight = DutyCalculations.calculateDutyHours("22:00", "06:00")
    assertEquals(8.0, overnight, 0.01)
  }
}
