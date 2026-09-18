package com.aistudio.lookia

import com.aistudio.lookia.data.model.DailyInspirationProvider
import com.aistudio.lookia.data.model.SecondChanceItem
import org.junit.Assert.*
import org.junit.Test

class DailyInspirationTest {
  @Test
  fun dailyInspirationProvider_returnsValidMessage() {
    val message = DailyInspirationProvider.getDailyMessage()
    assertNotNull(message)
    assertTrue(message.title.isNotBlank())
    assertTrue(message.biblicalOrWisdomQuote.isNotBlank())
    assertTrue(message.reflection.isNotBlank())
    assertTrue(message.affirmation.isNotBlank())
    assertTrue(message.practicalAct.isNotBlank())
  }

  @Test
  fun dailyInspirationProvider_allMessagesHaveThemesAndQuotes() {
    val messages = DailyInspirationProvider.getAllMessages()
    assertTrue(messages.size >= 10)
    messages.forEach {
      assertTrue("Day ${it.dayNumber} must have title", it.title.isNotBlank())
      assertTrue("Day ${it.dayNumber} must have reflection", it.reflection.isNotBlank())
    }
  }

  @Test
  fun secondChanceItem_creationAndAttributes() {
    val item = SecondChanceItem(
      ownerName = "Camila",
      ownerAvatarBody = "Reloj de arena",
      title = "Blusa de lino",
      category = "Tops",
      actionType = "Regalo con amor",
      size = "M",
      condition = "Excelente estado",
      color = "Blanco",
      colorHex = "#FFFFFF",
      exchangeOrPrice = "Gratis",
      story = "Para alguien especial",
      location = "Bogotá"
    )
    assertEquals("Regalo con amor", item.actionType)
    assertEquals(0, item.interestedCount)
    assertFalse(item.isUserInterested)
  }
}

