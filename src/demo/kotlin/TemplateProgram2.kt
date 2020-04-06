import org.openrndr.application
import org.openrndr.panel.controlManager
import org.openrndr.panel.elements.Range
import org.openrndr.panel.elements.Slider
import org.openrndr.panel.elements.slider
import org.openrndr.extra.keyframer.Keyframer
import org.openrndr.extra.keyframer.KeyframerFormat
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

        var clockOffset = 0.0
        val oldClock = clock
        clock = { oldClock() - clockOffset }

        var clockSlider: Slider? = null

        val cm = controlManager {
            layout {
                clockSlider = slider {
                    range = Range(0.0, 30.0)
                    events.valueChanged.listen {
                        if (it.interactive) {
                            clockOffset = oldClock() - it.newValue
                        }
                    }
                }
            }
        }

        extend(cm)

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
            clockSlider?.value = seconds


            kf(seconds)
            drawer.fill = kf.color
            drawer.circle(kf.position, kf.radius)
        }
    }
}