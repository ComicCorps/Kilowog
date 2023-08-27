package github.buriedincode.kilowog

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.div
import kotlin.io.path.inputStream
import kotlin.io.path.pathString

object ZipUtils {
    fun unzip(srcFile: Path, destFolder: Path) {
        ZipInputStream(srcFile.inputStream()).use { `in` ->
            var ze: ZipEntry?
            while (`in`.nextEntry.also { ze = it } != null) {
                val filename: String = if (ze!!.name.startsWith("/")) {
                    ze!!.name.drop(1)
                } else {
                    ze!!.name
                }
                var destFile: Path = destFolder / filename
                destFile = destFile.normalize()
                if (!destFile.startsWith(destFolder)) {
                    throw RuntimeException("Entry with an illegal path: ${ze!!.name.subSequence(1, ze!!.name.length)}")
                }
                if (ze!!.isDirectory) {
                    Files.createDirectories(destFile)
                } else {
                    Files.createDirectories(destFile.parent)
                    Files.copy(`in`, destFile)
                }
            }
        }
    }

    fun zip(destFile: Path, content: List<Path>) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(destFile.toFile()))).use { out ->
            content.map { it.pathString }.forEach { file ->
                FileInputStream(file).use {
                    BufferedInputStream(it).use {
                        val entry = ZipEntry(file.substring(file.lastIndexOf("/")))
                        out.putNextEntry(entry)
                        it.copyTo(out, 1024)
                    }
                }
            }
        }
    }
}
