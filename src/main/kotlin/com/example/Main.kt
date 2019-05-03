package com.example

import app.app
import kotlinext.js.requireAll
import kotlinx.css.TextAlign
import kotlinx.css.margin
import kotlinx.css.padding
import kotlinx.css.px
import react.dom.div
import react.dom.render
import styled.css
import styled.styledDiv
import kotlin.browser.document

fun main(args: Array<String>) {


    if (document.body != null) {
        console.log("DOM Exist")
    } else {
        document.addEventListener("DOMContentLoaded", {
            console.log("DOM FOUND")
            requireAll(kotlinext.js.require.context("kotlin", true, js("/\\.css$/")))
            render(document.getElementById("kvapp")) {
                styledDiv{
                    css {
                        margin(0.px)
                        padding(0.px)
                        fontFamily="sans-serif"
                        textAlign= TextAlign.center
                    }
                    app()
                }
            }
        })
    }
}

