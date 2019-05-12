package com.antonio.cursedbot.app

import com.antonio.cursedbot.highscores.highscore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.json
import com.antonio.cursedbot.logo.logo
import com.antonio.cursedbot.voting.vote
import kotlinx.coroutines.await
import react.*
import react.dom.div
import react.dom.h2
import kotlinx.css.*
import styled.*

interface AppState : RState {
    var fbPhotosRef:dynamic
    var telegramClient: dynamic
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
            InitializeClients()
        }
    }


    fun InitializeClients(){
        val firebase = kotlinext.js.require("firebase")
        firebase.initializeApp(config)
        val firebaseref = firebase.database().ref("photos")

        val TelegramClient = kotlinext.js.require("messaging-api-telegram").TelegramClient

        val telegramclient = TelegramClient.connect("727995564:AAGnvmbhmIpyBCXecDtmSg1CqRzyAWg4xEA")

        setState {
            fbPhotosRef=firebaseref
            telegramClient=telegramclient
        }
    }

    override fun RBuilder.render() {

        styledDiv {
            css {
                margin(0.px)
                padding(0.px)
                fontFamily = "sans-serif"
                textAlign = TextAlign.center
                height = LinearDimension.fillAvailable
                display = Display.flex
                flexDirection = FlexDirection.column
            }


            styledHeader {
                css {
                    backgroundColor = Color.chocolate
                    flex(1.0, 1.0)
                    borderRadius = 10.px
                    margin(8.px)
                    minHeight = 10.em
                }

                styledDiv {
                    css {
                        backgroundColor = Color.black
                        color = Color.white
                        height = 100.pct
                    }
                    logo()
                    h2 {
                        +"Welcome to React with Kotlin"
                    }
                }
            }

            styledDiv {

                css {
                    flex(3.0, 3.0)
                    display = Display.flex
                    flexDirection = FlexDirection.row
                }

                styledSection {
                    css {
                        backgroundColor = Color.coral
                        flex(1.0, 1.0)
                        borderRadius = 10.px
                        margin(8.px)
                        display = Display.flex
                        flexDirection = FlexDirection.row
                        alignItems = Align.center
                        justifyContent = JustifyContent.spaceEvenly
                    }

                    if (state.fbPhotosRef != null && state.telegramClient != null){
                        vote(state)
                    }
                }

                styledAside {
                    css {
                        backgroundColor = Color.aliceBlue
                        flex(1.0, 1.0)
                        maxWidth = 35.em
                        borderRadius = 10.px
                        margin(8.px)
                    }

                    if (state.fbPhotosRef != null && state.telegramClient != null) {
                        div {
                            highscore(state)
                        }
                    }
                }
            }

            styledFooter {
                css {
                    backgroundColor = Color.yellowGreen
                    flex(1.0, 1.0)
                    borderRadius = 10.px
                    margin(8.px)
                    minHeight = 10.em
                }
            }
        }
    }
}

fun RBuilder.app() = child(App::class) {}
