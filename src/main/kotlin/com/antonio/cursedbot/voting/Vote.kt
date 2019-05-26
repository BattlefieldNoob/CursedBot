package com.antonio.cursedbot.voting

import com.antonio.cursedbot.app.AppState
import kotlinext.js.Object
import kotlinext.js.asJsObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.css.properties.*
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.json.json
import react.*
import react.dom.div
import styled.StyleSheet
import styled.css
import styled.styledDiv
import styled.styledImg
import kotlin.js.Promise
import kotlin.random.Random

interface VoteProps : RProps {
    var appState: AppState
}

interface VoteState : RState {
    var MaxIndex: Int
    var votingFileIds: List<String>
    var votingUrls: List<String>
    var isValid: Boolean
    var voted:Boolean
}

class Vote(props: VoteProps) : RComponent<VoteProps, VoteState>(props) {

    lateinit var first:ReactElement
    lateinit var second:ReactElement

    object base : StyleSheet("VotingImages") {
        val wrapper by  css {
            height = 35.em
            width = 35.em
            margin(1.em)
            transform.rotateY(0.deg)
            opacity=100

            objectFit = ObjectFit.contain
           /* hover {
                transform {
                    scale(1.05)

                }
            }*/

            transition(duration = 0.5.s)
        }
    }

    object hover:StyleSheet("hover"){
        val wrapper by css{
            hover {
                transform {
                    scale(1.05)
                }
            }
        }
    }

    object clicked : StyleSheet("clicked") {
        val wrapper by  css {
            transform.rotateY(90.deg)
            opacity=0
            /*hover {
                transform {
                    scale(1)
                }
            }*/
        }
    }

    override fun componentWillMount() {
        GlobalScope.launch {
            getMaxIndex()
            getVoting()
        }
    }


    private suspend fun getVoting(){
        val fileIds = getVotingImages()
        val urls = getUrlfromFileIds(fileIds)
        setState {
            votingFileIds = fileIds
            votingUrls = urls
            voted=false
        }
    }

    private suspend fun getMaxIndex() {
        var maxIndexData = (props.appState.fbPhotosRef.orderByChild("index").limitToLast(1).once("value") as Promise<dynamic>).await()
        val snapMaxIndex = js("maxIndexData.val()")
        console.log(snapMaxIndex)
        console.log(snapMaxIndex[Object.keys(snapMaxIndex)[0]]["index"])
        val maxIndex = snapMaxIndex[Object.keys(snapMaxIndex)[0]]["index"]
        setState {
            MaxIndex = maxIndex
        }
    }

    private suspend fun getUrlfromFileIds(fileIds: List<String>): List<String> {
        val urls = mutableListOf<String>()
        fileIds.forEach {
            val link = (props.appState.telegramClient.getFileLink(it) as Promise<dynamic>).await()
            urls.add(link)
        }
        return urls.toList()
    }


    private suspend fun getVotingImages(): List<String> {

        val first = Random.nextInt(state.MaxIndex)
        var secondPhoto = first
        while (secondPhoto == first) {

            secondPhoto = Random.nextInt(state.MaxIndex)
        }

        var datafirst = (props.appState.fbPhotosRef.orderByChild("index").equalTo(first).once("value") as Promise<dynamic>).await()
        var datasecond = (props.appState.fbPhotosRef.orderByChild("index").equalTo(secondPhoto).once("value") as Promise<dynamic>).await()
        val snapfirst = js("datafirst.val()")
        val snapsecond = js("datasecond.val()")
        return listOf(Object.keys(snapfirst)[0], Object.keys(snapsecond)[0])
    }

    private fun voteFor(voteForIndex: Int) {
        val votingFor = state.votingFileIds[voteForIndex]
        console.log("Vote for:$votingFor")

        GlobalScope.launch {
            console.log("coroutine")
            val votingChild=props.appState.fbPhotosRef.child(votingFor)
            val votedData = (votingChild.once("value") as Promise<dynamic>).await()
            console.log("getData")
            val snapfirst = js("votedData.val()")

            console.log(snapfirst)

            var score = 0

            if (snapfirst.hasOwnProperty("score")) {
                score = snapfirst["score"]
                console.log(score)
            }

            score++

            val updateValue = JSON.parse<Any>(json {
                "score" to score
            }.toString())

            val result=(votingChild.update(updateValue) as Promise<dynamic>).await()

            console.log(result)

            getVoting()
        }

    }

    override fun RBuilder.render() {
        div {
            if (state.votingUrls != null) {
                first=styledImg(src = state.votingUrls[0]) {
                    css {
                        +base.wrapper
                        if(state.voted){
                            +clicked.wrapper
                        }else{
                            +hover.wrapper
                        }
                    }
                    attrs {
                        onClickFunction = {
                            voteFor(0)
                            setState {
                                voted=true
                            }
                        }
                    }

                }

                second=styledImg(src = state.votingUrls[1]) {
                    css {
                        +base.wrapper
                        if(state.voted){
                            +clicked.wrapper
                        }else{
                            +hover.wrapper
                        }
                    }
                    attrs {
                        onClickFunction = {
                            voteFor(1)
                            setState {
                                voted=true
                            }
                        }

                    }
                }
            }
        }
    }
}

fun RBuilder.vote(appState: AppState) = child(Vote::class) {
    attrs.appState = appState
}