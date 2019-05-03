package app

import cursed_highscores.highscore
import kotlinext.js.Object
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.Color
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.serialization.json.json
import logo.logo
import react.*
import react.dom.code
import react.dom.div
import react.dom.h2
import react.dom.p
import styled.css
import styled.styledDiv
import ticker.ticker


interface AppState : RState {
    var highscore: MutableList<String>
}

class App : RComponent<RProps, AppState>() {

    val config = JSON.parse<Any>(json {
        "apiKey" to "AIzaSyDyhyljLsjtr57iaKjQz25HFgurkG0oquE"
        "authDomain" to "cursedbot-cc0e6.firebaseapp.com"
        "databaseURL" to "https://cursedbot-cc0e6.firebaseio.com"
        "projectID" to "cursedbot-cc0e6"
        "storageBucket" to "cursedbot-cc0e6.appspot.com"
        "messagingSenderId" to 777652804757
    }.toString())


    override fun componentWillMount() {
        GlobalScope.launch {
            GetHighScore()
        }
    }


    suspend fun GetHighScore(){
        val firebase=kotlinext.js.require("firebase")

        firebase.initializeApp(config)

        firebase.database().ref("photos").limitToLast(10).once("value"){
            data->
            val snap = js("data.val()")
            //console.log(snap)
            val tmp= mutableListOf(*Object.keys(snap))

            setState{
                highscore=tmp
            }
        }
    }

    override fun RBuilder.render() {

        styledDiv {
            css {
                backgroundColor = Color.black
                height = 160.px
                padding(20.px)
                color = Color.white
            }
            logo()
            h2 {
                +"Welcome to React with Kotlin"
            }
        }
        p {
            +"To get started, edit "
            code { +"app/App.kt" }
            +" and save to reload."
        }
        p("App-ticker") {
            ticker()
        }
        if(state.highscore!=null) {
            div("Bing") {
                highscore(state.highscore)
            }
        }
    }
}

fun RBuilder.app() = child(App::class) {}
