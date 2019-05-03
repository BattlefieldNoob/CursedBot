package bing

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.css.pct
import kotlinx.serialization.json.json
import react.*
import styled.css
import styled.styledImg
import kotlin.js.JSON.stringify
import kotlin.js.Promise

interface Propsss : RProps

interface HorseImageState : RState {
    var currentImageUrl: String
}

class HorseImageVisualizer(props: Propsss) : RComponent<Propsss, HorseImageState>(props) {


    val config = JSON.parse<Any>(json {
        "apiKey" to "AIzaSyDyhyljLsjtr57iaKjQz25HFgurkG0oquE"
        "authDomain" to "cursedbot-cc0e6.firebaseapp.com"
        "databaseURL" to "https://cursedbot-cc0e6.firebaseio.com"
        "projectID" to "cursedbot-cc0e6"
        "storageBucket" to "cursedbot-cc0e6.appspot.com"
        "messagingSenderId" to 777652804757
    }.toString())

    override fun RBuilder.render() {
        styledImg(src = state.currentImageUrl) {
            css {
                height=75.pct
                width=75.pct
            }
        }
    }

    override fun componentWillMount() {

        GlobalScope.launch {
            executeGet()
        }
    }

    suspend fun executeGet() {
        val Clas=kotlinext.js.require("google-images")
        val inst=kotlin.js.js("new Clas(\"002303778391620413548:mfvt4i1diwc\",\"AIzaSyCIP-2MfmfH8rkeYk-RdxVWlxWYhLY9bC8\")")

        val array=(inst.search("black horse") as Promise<Array<dynamic>>).await()



        val firebase=kotlinext.js.require("firebase")

        firebase.initializeApp(config)

        firebase.database().ref().child("photos").push(JSON.parse<Any>(json {
            "username" to "Hello World"
        }.toString()))



        //val arrayResult = GoogleImageSearch.searchImage("black horse").await()
        console.log(array)
        setState {
            currentImageUrl = array.random().url
        }
    }

}

fun RBuilder.horseImageVisualizer() = child(HorseImageVisualizer::class) {

}