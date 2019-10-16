/*
 * Copyright (c) 2019 Mathias Doenitz
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.bullet.borer.input

import java.io.InputStream

import io.bullet.borer._
import utest._

import scala.util.Random

object FromInputStreamInputSpec extends TestSuite with TestUtils {

  val random = new Random

  val tests = Tests {

    "FromInputStreamInput" - {

      def newBytesIterator = Iterator.from(0).map(_.toByte).take(10000)

      val bytes = newBytesIterator
      val inputStream = new InputStream {
        def read() = ???
        override def read(b: Array[Byte]) =
          if (bytes.hasNext) {
            if (random.nextInt(4) == 0) {
              b.indices.iterator
                .take(random.nextInt(256))
                .takeWhile(_ => bytes.hasNext)
                .map { ix =>
                  b(ix) = bytes.next()
                  ix
                }
                .max + 1
            } else 0
          } else -1
      }

      val input = Input.fromInputStream(inputStream, bufferSize = 300)

      val paddingProvider = new Input.PaddingProvider[Array[Byte]] {
        def padByte()                                  = 42
        def padDoubleByte(remaining: Int)              = ???
        def padQuadByte(remaining: Int)                = ???
        def padOctaByte(remaining: Int)                = ???
        def padBytes(rest: Array[Byte], missing: Long) = ???
      }

      for {
        (a, b) <- newBytesIterator.map(_ -> input.readBytePadded(paddingProvider))
      } a ==> b

      input.cursor ==> 10000

      input.readBytePadded(paddingProvider) ==> 42
    }
  }
}
