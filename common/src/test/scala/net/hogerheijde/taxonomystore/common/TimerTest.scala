package net.hogerheijde.taxonomystore.common

import org.scalatest.{Matchers, WordSpec}

class TimerTest extends WordSpec with Matchers {
  "Timer" should {

    "return result of passed function" in {
      Timer("A").value should be("A")
      Timer(1).value should be(1)
      Timer('a').value should be('a')
    }

    "have some timing result" in {
      Timer(Thread.sleep(100)).duration.toMillis should be >= 100L
      Timer(Thread.sleep(200)).duration.toMillis should be >= 200L
    }
  }
}
