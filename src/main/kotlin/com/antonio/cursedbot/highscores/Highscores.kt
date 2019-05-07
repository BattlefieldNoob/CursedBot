package com.antonio.cursedbot.highscores

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.css.*
import react.*
import react.dom.div
import react.dom.img
import react.dom.li
import react.dom.ol
import styled.css
import styled.styledDiv
import styled.styledImg
import styled.styledOl
import kotlin.js.Promise

interface HighscoreProps : RProps{
    var toptenProps: List<String>
}

interface HighscoreState : RState {
    var toptenImgLink: MutableList<String>
}

class Highscore(props: HighscoreProps) : RComponent<HighscoreProps, HighscoreState>(props) {


    override fun componentWillMount() {
        GlobalScope.launch {
            telegramRequest()
        }
    }


    suspend fun telegramRequest(){

        /*
        val TelegramClient=kotlinext.js.require("messaging-api-telegram").TelegramClient

        val client = TelegramClient.connect("727995564:AAGnvmbhmIpyBCXecDtmSg1CqRzyAWg4xEA")

        val user=(client.getMe() as Promise<dynamic>).await()

        console.log("MyID ${user.id}")

         */
        val tmp= mutableListOf<String>()

        for (file_id in props.toptenProps) {
            //val link=(client.getFileLink(file_id) as Promise<dynamic>).await()
            val link=file_id
            console.log(link)
            tmp.add(link)
        }

        setState {
            toptenImgLink=tmp
        }

    }

    override fun RBuilder.render() {

        styledDiv {
            css {
                width=100.pct
                height=100.pct
                flexDirection=FlexDirection.row
                display=Display.inlineFlex
                backgroundColor=Color.green
                flex(1.0)
            }

            /*styledDiv {
                css {
                    width = 10.px
                    height = 90.pct
                    backgroundColor = Color.gray
                    flex(1.0)
                }
            }*/

            styledDiv {
                css {
                    flex(1.0,1.0)
                }
                styledOl {
                    css {
                        width=100.pct
                        listStyleType=ListStyleType.none
                        padding(0.px)
                    }
                    if(state.toptenImgLink!=null){
                        li {
                            for (imglink in state.toptenImgLink) {
                                styledDiv {
                                    css{
                                        height=15.em
                                        width=100.pct
                                        backgroundColor= Color.red
                                        flex(1.0)
                                        flexDirection=FlexDirection.row
                                        display=Display.inlineFlex

                                    }
                                    styledDiv {
                                        css{
                                            backgroundColor= Color.black
                                            height=13.em
                                            width=13.em
                                            margin(1.em)
                                        }
                                    }


                                    styledDiv {
                                        css {
                                            flex(1.0)
                                            width=5.em
                                            textAlign=TextAlign.center
                                            fontSize=8.pt
                                            alignSelf = Align.center
                                        }
                                        + "Fist Place"
                                    }
                                }

                            }
                        }
                    }
                }
            }

        }


    }
}


fun RBuilder.highscore(topTen: List<String>) = child(Highscore::class) {
    attrs.toptenProps=topTen
}