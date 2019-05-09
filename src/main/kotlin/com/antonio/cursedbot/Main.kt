package com.antonio.cursedbot

import com.antonio.cursedbot.app.app
import kotlinext.js.requireAll
import kotlinx.css.*
import react.dom.render
import styled.css
import styled.styledDiv
import kotlin.browser.document

fun main(args: Array<String>) {


    if (document.body != null) {
        console.log("DOM Exist")
    } else {
        document.addEventListener("DOMContentLoaded", {
            requireAll(kotlinext.js.require.context("kotlin", true, js("/\\.css$/")))
            render(document.getElementById("kvapp")) {
               app()
            }
        })
    }
}

