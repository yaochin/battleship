package com.yaochin.battleship.util

import com.yaochin.battleship.domain.Location
import org.scalatest.{FreeSpec, Matchers}

/**
  * Created on 1/31/17.
  */
class JsonSupportTest extends FreeSpec with JsonSupport with Matchers{

  "fromJson" - {
    "primitive class" in {
      // Given
      val expect = Location(1,2)

      // When
      val actual = fromJson[Location]("""{"x":1, "y":2}""")

      // Then
      actual should be(expect)
    }

    "nested class" in {
      // Given
      val expect = TestObj(1, "desc", Location(1,2))

      // When
      val actual = fromJson[TestObj]("""{"id":1, "desc": "desc", "coordinate": {"x":1, "y":2}}""")

      // Then
      actual should be(expect)
    }

    "seq" in {
      // Given
      val expect = List(Location(1,1), Location(2,2))

      // When
      val actual = fromJson[List[Location]]("""[{"x":1, "y":1}, {"x":2, "y":2}]""")

      // Then
      actual should be(expect)
    }

    "some" in {
      // Given
      val expect = TestObj2(id = 1, desc = Some("text"))

      // When
      val actual = fromJson[TestObj2]("""{"id": 1, "desc": "text"}""")

      // Then
      actual should be(expect)
    }

    "none" in {
      // Given
      val expect = TestObj2(id = 1, desc = None)

      // When
      val actual = fromJson[TestObj2]("""{"id": 1}""")

      // Then
      actual should be(expect)
    }
  }

  "toJson" - {
    "basic" in {
      // Given
      val test = TestObj(1, "test", Location(1,2))

      // When / Then
      toJson[TestObj](test) should be("""{"id":1,"desc":"test","coordinate":{"x":1,"y":2}}""")
    }


  }


}

case class TestObj(id: Int, desc: String, coordinate: Location)
case class TestObj2(id: Int, desc: Option[String])
