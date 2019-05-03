package cursed_highscores

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import react.*
import react.dom.h1
import react.dom.img
import react.dom.li
import react.dom.ol
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

        val TelegramClient=kotlinext.js.require("messaging-api-telegram").TelegramClient

        val client = TelegramClient.connect("727995564:AAGnvmbhmIpyBCXecDtmSg1CqRzyAWg4xEA")

        val user=(client.getMe() as Promise<dynamic>).await()

        console.log("MyID ${user.id}")

        val tmp= mutableListOf<String>()

        for (file_id in props.toptenProps) {
            val link=(client.getFileLink(file_id) as Promise<dynamic>).await()
            console.log(link)
            tmp.add(link)
        }

        setState {
            toptenImgLink=tmp
        }

    }

    override fun RBuilder.render() {
        ol {
            if(state.toptenImgLink!=null){
                li {
                    for (imglink in state.toptenImgLink) {
                        img(src = imglink) {
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