
@file:JsModule("react-flip-move")

package com.antonio.cursedbot.highscores

import react.*

external interface FlipMoveProps : RProps {
    var className: String
    var align : String
    var color : String
    var variant : String
    var gutterBottom : String
    var noWrap : String
    var paragraph : String
}

abstract external class FlipMove : Component<dynamic, RState> {
    override fun render(): ReactElement
}
