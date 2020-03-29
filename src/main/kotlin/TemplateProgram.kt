import org.openrndr.application
import org.operndr.extra.keyframer.Keyframer
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
        watchFile(File("data/keyframes/circle-expressions.json")) {
            kf.loadFromJson(it)
        }

        extend {
            kf(seconds)
            drawer.fill = kf.color
            drawer.circle(kf.position, kf.radius)
        }
    }
}