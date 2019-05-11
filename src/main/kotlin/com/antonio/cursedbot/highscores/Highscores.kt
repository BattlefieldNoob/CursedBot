package com.antonio.cursedbot.highscores

import com.antonio.cursedbot.app.AppState
import kotlinext.js.Object
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.css.properties.scale
import kotlinx.css.properties.transform
import react.*
import react.dom.li
import styled.css
import styled.styledDiv
import styled.styledImg
import styled.styledOl
import kotlin.js.Promise
import kotlin.random.Random

interface HighscoreProps : RProps{
    var appState:AppState
}

interface HighscoreState : RState {
    var topImgLink: List<String>
}

class Highscore(props: HighscoreProps) : RComponent<HighscoreProps, HighscoreState>(props) {


    override fun componentWillMount() {
        GlobalScope.launch {
            val top5FileIds=GetHighscoreFileIds()
            val urls=getUrlfromFileIds(top5FileIds)
            setState{
                topImgLink=urls
            }
        }
    }

    suspend fun getUrlfromFileIds(fileIds:List<String>):List<String>{
        val urls= mutableListOf<String>()
        fileIds.forEach {
            val link = (props.appState.telegramClient.getFileLink(it) as Promise<dynamic>).await()
            urls.add(link)
        }
        return urls.toList()
    }

    suspend fun GetHighscoreFileIds():List<String> {
        val top5 = (props.appState.fbPhotosRef.orderByChild("score").limitToLast(5).once("value") as Promise<dynamic>).await()

        val snap = js("top5.val()")
        console.log(snap)
        //console.log(snap)
        return listOf(*Object.keys(snap))
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
                    if(state.topImgLink!=null){
                        li {
                            for (imglink in state.topImgLink) {
                                styledDiv {
                                    css{
                                        height=15.em
                                        width=100.pct
                                        backgroundColor= Color.red
                                        flex(1.0)
                                        flexDirection=FlexDirection.row
                                        display=Display.inlineFlex
                                    }
                                    styledImg(src = imglink){
                                        css{
                                            height=13.em
                                            width=13.em
                                            objectFit=ObjectFit.contain
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
                                        + "First Place"
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


fun RBuilder.highscore(appState: AppState) = child(Highscore::class) {
    attrs.appState=appState
}