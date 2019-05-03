package logo

import kotlinx.css.Display
import kotlinx.css.Position
import kotlinx.css.pct
import kotlinx.css.properties.Timing.Companion.linear
import kotlinx.css.properties.deg
import kotlinx.css.properties.rotateY
import kotlinx.css.properties.s
import kotlinx.css.properties.times
import react.RBuilder
import react.dom.jsStyle
import styled.animation
import styled.css
import styled.styledDiv
import styled.styledImg

@JsModule("react.svg")
@JsNonModule
external val reactLogo: dynamic
@JsModule("kotlin.svg")
@JsNonModule
external val kotlinLogo: dynamic

fun RBuilder.logo(height: Int = 100) {
    styledDiv {
        css {
            position = Position.relative
            display = Display.inlineBlock
        }
        attrs.jsStyle.height = height
        //img(alt = "React logo.logo", src = reactLogo as? String, classes = "Logo-react") {}
        styledImg(alt = "React logo.logo", src = reactLogo as? String) {
            css {
                //animation(handler = LogoSpin.wrapper,duration= 10.s, delay = 5.s)
                animation(duration = 10.s,timing = linear,iterationCount = 100.times){
                    //LogoSpin.wrapper
                    from { transform.rotateY(270.deg); opacity = 1 }

                    rule("50%") {
                        transform.rotateY(90.deg)
                        opacity = 1
                    }

                    rule("51%") {
                        transform.rotateY(90.deg)
                        opacity = 0
                    }

                    to { transform.rotateY(270.deg); opacity = 0 }
                }
                css.height = 100.pct
            }
        }


        styledImg(alt = "Kotlin logo.logo", src = kotlinLogo as? String) {
            css {
                opacity = 0
                //animation(handler = LogoSpin.wrapper,duration= 10.s, delay = 5.s)
                animation(duration = 10.s,timing = linear,delay = 5.s,iterationCount = 100.times){
                    //LogoSpin.wrapper
                    from { transform.rotateY(270.deg); opacity = 1 }

                    rule("50%") {
                        transform.rotateY(90.deg)
                        opacity = 1
                    }

                    rule("51%") {
                        transform.rotateY(90.deg)
                        opacity = 0
                    }

                    to { transform.rotateY(270.deg); opacity = 0 }
                }
                css.height = 72.pct
                position = Position.absolute
                top = 14.pct
                right = 24.pct
            }
        }
    }
}
