package com.antonio.cursedbot.app

import com.antonio.cursedbot.highscores.highscore
import kotlinext.js.Object
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.json
import com.antonio.cursedbot.logo.logo
import kotlinx.coroutines.await
import react.*
import react.dom.div
import react.dom.h2
import kotlinx.css.*
import styled.*
import kotlin.js.Promise
import kotlin.random.Random

interface AppState : RState {
    var clientt:Any
    var highscore: MutableList<String>
    var firstid:String
    var secondid:String
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

        val TelegramClient=kotlinext.js.require("messaging-api-telegram").TelegramClient

        val client = TelegramClient.connect("727995564:AAGnvmbhmIpyBCXecDtmSg1CqRzyAWg4xEA")

        val highscores= mutableListOf<String>()
        firebase.database().ref("photos").limitToLast(5).once("value"){
            data->
            val snap = js("data.val()")
            //console.log(snap)
            highscores.addAll(Object.keys(snap))

            var index = -1

            firebase.database().ref("photos").orderByChild("index").limitToLast(1).once("value"){ data ->

                        val snap = js("data.val()")
                        console.log(snap)
                        console.log(snap[Object.keys(snap)[0]]["index"])
                        val maxIndex = snap[Object.keys(snap)[0]]["index"]
                        var first=Random.nextInt(maxIndex)
                var secondPhoto = Random.nextInt(maxIndex)

                while (secondPhoto == first) {

                    secondPhoto = Random.nextInt(maxIndex)
                }

                firebase.database().ref("photos").orderByChild("index").equalTo(first).once("value"){ datafirst->
                    firebase.database().ref("photos").orderByChild("index").equalTo(secondPhoto).once("value"){
                        datasecond->
                        val datafirst1=datafirst
                        val snapfirst = js("datafirst1.val()")
                        val snapsecond = js("datasecond.val()")
                        val idFirst=Object.keys(snapfirst)[0]
                        val idSecond=Object.keys(snapsecond)[0]

                        GlobalScope.launch {
                            val link = (client.getFileLink(idFirst) as Promise<dynamic>).await()
                            val link1 = (client.getFileLink(idSecond) as Promise<dynamic>).await()
                            val telegramclient=client
                            setState{
                                clientt=telegramclient
                                highscore=highscores
                                firstid=link
                                secondid=link1
                            }
                        }



                    }
                }
            }
        }



        /*console.log(db.photos)

        val tmp= mutableListOf(*Object.keys(db.photos))


        setState{
            highscore=tmp.take(3).toMutableList()
        }*/
    }

    override fun RBuilder.render() {

        styledDiv {
            css {
                margin(0.px)
                padding(0.px)
                fontFamily = "sans-serif"
                textAlign = TextAlign.center
                height = LinearDimension.fillAvailable
                display=Display.flex
                flexDirection=FlexDirection.column
            }


            styledHeader {
                css {
                    backgroundColor = Color.chocolate
                    flex(1.0,1.0)
                    borderRadius=10.px
                    margin(8.px)
                    minHeight=10.em
                }

                styledDiv {
                    css {
                        backgroundColor = Color.black
                        color = Color.white
                        height=100.pct
                    }
                    logo()
                    h2 {
                        +"Welcome to React with Kotlin"
                    }
                }
            }

            styledDiv {

                css {
                    flex(3.0,3.0)
                    display=Display.flex
                    flexDirection=FlexDirection.row
                }

                styledSection {
                    css {
                        backgroundColor = Color.coral
                        flex(1.0, 1.0)
                        borderRadius = 10.px
                        margin(8.px)
                        display=Display.flex
                        flexDirection=FlexDirection.row
                        alignItems=Align.center
                        justifyContent=JustifyContent.spaceEvenly
                    }

                    styledImg(src = state.firstid) {
                        css{
                            height=35.em
                            width=35.em
                            margin(1.em)
                            objectFit=ObjectFit.contain
                        }
                    }

                    styledImg(src = state.secondid)  {
                        css{
                            height=35.em
                            width=35.em
                            margin(1.em)
                            objectFit=ObjectFit.contain
                        }
                    }
                }

                styledAside {
                    css {
                        backgroundColor = Color.aliceBlue
                        flex(1.0, 1.0)
                        maxWidth=35.em
                        borderRadius = 10.px
                        margin(8.px)
                    }

                    if(state.highscore!=null) {
                        div("Bing") {
                            highscore(state.highscore,state.clientt)
                        }
                    }
                }
            }

            styledFooter {
                css {
                    backgroundColor = Color.yellowGreen
                    flex(1.0,1.0)
                    borderRadius=10.px
                    margin(8.px)
                    minHeight=10.em
                }
            }
        }
        /*styledDiv {
            css{
                flex(1.0)
                backgroundColor= Color.aqua
            }

            styledSection {
                css {
                    backgroundColor=Color.coral
                    float=Float.left
                }
            }

        }*/
    }
}

fun RBuilder.app() = child(App::class) {}
