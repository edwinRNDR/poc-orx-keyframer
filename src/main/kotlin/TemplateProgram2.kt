import org.openrndr.application
import org.operndr.extra.keyframer.Keyframer
import org.operndr.extra.keyframer.KeyframerFormat
import org.operndr.extras.filewatcher.watchFile
import java.io.File

fun main() = application {
    configure {
        width = 768
        height = 576
    }

    program {
        class Keyframe : Keyframer() {
            val position by Vector2Channel(arrayOf("x", "y"))
            val radius by DoubleChannel("radius")
            val color by RGBChannel(arrayOf("r", "g", "b"))
        }

        val kf = Keyframe()
        watchFile(File("data/keyframes/circle-parametric-2.json")) {
            kf.loadFromJson(
                it,
                KeyframerFormat.FULL,
                mapOf(
                    "width" to width*1.0,
                    "height" to height * 1.0
                )
            )
        }

        extend {
            kf(seconds)
            drawer.fill = kf.color
            drawer.circle(kf.position, kf.radius)
        }
    }
}