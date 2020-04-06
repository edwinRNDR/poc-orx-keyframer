# poc-orx-keyframer
Proof-of-concept for a reusable keyframer

I worked out a POC for a highly re-usable keyframer. I've tried many different ways, including even annotation processing, and I have found this is by far the cleanest and simplest. It heavily relies on property delegation -which I've only realized recently is incredibly flexible.

This POC relies on JSON files, but that's not a hard dependency, it can be replaced with any deserializer scheme.

What this allows you to do:

1. Create a keyframed animation in a json file.

```json
[
  {
    "time": 0.0,
    "easing": "cubic-in-out",
    "x": 3.0,
    "y": 4.0,
    "z": 9.0,
    "r": 0.1,
    "g": 0.5,
    "b": 0.2,
    "radius": 50
  },
  {
    "time": 2.0,
    "easing": "cubic-in-out",
    "r": 0.6,
    "g": 0.5,
    "b": 0.1
  },
  {
    "time": 4.0,
    "easing": "cubic-in-out",
    "x": 10.0,
    "y": 4.0,
    "radius": 400
  },
  {
    "time": 5.0,
    "easing": "cubic-in-out",
    "x": 100.0,
    "y": 320.0,
    "radius": 400
  },
  {
    "time": 5.3,
    "easing": "cubic-in-out",
    "x": 100.0,
    "y": 320.0,
    "radius": 40
  }
]
```

2. Map the animation data to Kotlin types:

```kotlin
class Animation : Keyframer() {
    val position by Vector2Channel(arrayOf("x", "y"))
    val radius by DoubleChannel("radius")
    val color by RGBChannel(arrayOf("r", "g", "b"))
}

val animation = Animation()
animation.loadFromJson(File("data/keyframes/animation.json"))
```

3. Animate! (from an OPENRNDR program)

```kotlin
extend {
    animation(seconds)
    drawer.fill = animation.color
    drawer.circle(animation.position, animation.radius)
}
```

## Advanced features

[Repeats](data/keyframes/circle-expressions.json), simple key repeating mechanism

[Expressions](data/keyframes/circle-expressions.json), expression mechanism. Currently uses values `r` to indicate repeat index and `t` the last used key time, `v` the last used value (for the animated attribute).

Supported functions in expressions:
 - `min(x, y)`, `max(x, y)`
 - `cos(x)`, `sin(x)`, `acos(x)`, `asin(x)`, `tan(x)`, `atan(x)`, `atan2(y, x)`
 - `abs(x)`, `saturate(x)`
 - `degrees(x)`, `radians(x)`
 - `pow(x,y)`, `sqrt(x)`, `exp(x)`
 - `mix(left, right, x)`
 - `smoothstep(t0, t1, x)`
 - `map(leftBefore, rightBefore, leftAfter, rightAfter, x)`
 - `random()`, `random(min, max)`
  
[Parameters and prototypes](data/keyframes/circle-parametric-2.json)


## Using this as a library

 - clone and run `./gradlew install`
 - in an openrndr-template based project set `orxUseSnapshot` to `true` (make sure you have a recent snapshot installed)
 - add `poc-orx-keyframer` to `openrndrFeatures`
