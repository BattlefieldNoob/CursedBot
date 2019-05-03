package telegram

import me.ivmg.telegram.HandleUpdate
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.Dispatcher
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.dispatcher.handlers.Handler
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import com.google.firebase.FirebaseApp
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import java.io.FileInputStream
import com.google.firebase.database.FirebaseDatabase
import me.ivmg.telegram.Bot
import me.ivmg.telegram.dispatcher.callbackQuery
import me.ivmg.telegram.entities.*
import okhttp3.logging.HttpLoggingInterceptor
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap
import com.google.firebase.database.DataSnapshot
import java.time.Period
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer


fun Dispatcher.photo(body: HandleUpdate) {
    addHandler(PhotoHandler(body))
}


class PhotoHandler(handler: HandleUpdate) : Handler(handler) {
    override val groupIdentifier: String = "PhotoHandler"

    override fun checkUpdate(update: Update): Boolean {
        return (update.message?.photo != null)
    }
}


val inlineKeyboardMainMenu = InlineKeyboardMarkup(listOf(
        listOf(InlineKeyboardButton("Vote Pair",callbackData = "VoteByInline"), InlineKeyboardButton("Random",callbackData = "ShowRandomByInline")),
        listOf(InlineKeyboardButton("Highscore",callbackData = "HighscoreByInline")))
)

fun main() {

    val serviceAccount = FileInputStream("cursedbot-cc0e6-firebase.json")

    val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://cursedbot-cc0e6.firebaseio.com")
            .build()

    val app = FirebaseDatabase.getInstance(FirebaseApp.initializeApp(options))

    val collection = app.reference.child("photos")


    val idAndTimestamp = mutableMapOf<Long, LocalDateTime>()

    val voteSession = mutableMapOf<String, HashMap<String,Any>>()

    val query = collection
            .orderByChild("index")
            .limitToLast(1)

    var index = -1

    query.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            try {
                val data = dataSnapshot.value as HashMap<String, HashMap<String, Any>>
                index = data.values.first()["index"].toString().toInt() + 1

            } catch (e: Exception) {

            }
        }

        override fun onCancelled(dataError: DatabaseError) {

        }
    })

    index++

    val bot = bot {
        token = "727995564:AAGnvmbhmIpyBCXecDtmSg1CqRzyAWg4xEA"
        //logLevel=HttpLoggingInterceptor.Level.BASIC

        dispatch {

            command("start"){ bot, update ->
                bot.sendMessage(chatId = update.message!!.chat.id,text = "Welcome!",replyMarkup = inlineKeyboardMainMenu)
            }

            command("showrandom") { bot, update ->
                ShowRandomRequest(index, collection, bot, update.message!!.chat.id)
            }

            command("vote") { bot, update ->
                VoteRequest(index, collection, bot, update.message!!.chat.id, voteSession)
            }

            command("highscore"){ bot, update ->
                HighscoreRequest(collection, bot, update.message!!.chat.id)
            }

            callbackQuery("VoteByInline"){ bot,update->
                VoteRequest(index, collection, bot, update.callbackQuery!!.message!!.chat.id, voteSession)
            }

            callbackQuery("HighscoreByInline"){ bot,update->
                HighscoreRequest(collection, bot, update.callbackQuery!!.message!!.chat.id)
            }

            callbackQuery("ShowRandomByInline"){ bot,update->
                ShowRandomRequest(index, collection, bot, update.callbackQuery!!.message!!.chat.id)
            }


            callbackQuery { bot, update ->

                if(!update.callbackQuery!!.data.contains(":"))
                    return@callbackQuery

                val args=update.callbackQuery!!.data.split(":")

                val session=args[0]
                val voteForId=args[1]

                if(!voteSession.containsKey(session))
                    return@callbackQuery

                if((voteSession[session]!!["timestamp"] as LocalDateTime).until(LocalDateTime.now(),ChronoUnit.DAYS)>=1) {
                    bot.editMessageReplyMarkup(chatId = voteSession[session]!!["chatId"]as Long, messageId = voteSession[session]!!["firstMessageId"] as Long, replyMarkup = InlineKeyboardMarkup(listOf(listOf())))
                    bot.editMessageReplyMarkup(chatId = voteSession[session]!!["chatId"]as Long, messageId = voteSession[session]!!["secondMessageId"] as Long, replyMarkup = InlineKeyboardMarkup(listOf(listOf())))
                    return@callbackQuery
                }

                val messagesId=voteSession[session]

                val query = collection.child(voteForId)

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        try {
                            val data = dataSnapshot.value as HashMap<String, Any>
                            var score=0
                            if(data.containsKey("score")){
                                score=(data["score"] as Long).toInt()
                            }
                            score++

                            val hashMap = HashMap<String, Any>()
                            hashMap["score"] = score

                            query.updateChildrenAsync(hashMap)

                        } catch (e: Exception) {

                        }
                    }

                    override fun onCancelled(dataError: DatabaseError) {

                    }
                })

                bot.editMessageReplyMarkup(chatId = update.callbackQuery!!.message!!.chat.id,messageId = messagesId!!["firstMessageId"]as Long, replyMarkup = InlineKeyboardMarkup(listOf(listOf())))
                bot.editMessageReplyMarkup(chatId = update.callbackQuery!!.message!!.chat.id,messageId = messagesId!!["secondMessageId"]as Long, replyMarkup = InlineKeyboardMarkup(listOf(listOf())))
                bot.sendMessage(chatId = update.callbackQuery!!.message!!.chat.id,text = "Thank you for your vote",replyMarkup = inlineKeyboardMainMenu)

                voteSession.remove(session)

            }


            photo { bot, update ->
                val photo = update.message?.photo

                if (photo != null) {
                    val fileid = photo.sortedBy { it.fileSize }.last().fileId
                    println("Adding $fileid by ${update.message!!.from!!.firstName}")

                    val query = collection.child(fileid)

                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            try {
                                if (dataSnapshot.value == null) {
                                    println("Writing!")
                                    val hashMap = HashMap<String, Any>()
                                    hashMap["file_id"] = fileid
                                    hashMap["by"] = update.message!!.from!!.firstName
                                    hashMap["index"] = index
                                    hashMap["score"] = 0
                                    index++

                                    collection.child(fileid).setValueAsync(hashMap)
                                } else {
                                    println("File already exist!")
                                }
                            } catch (e: Exception) {

                            }
                        }

                        override fun onCancelled(dataError: DatabaseError) {

                        }
                    })

                    var lastUpdateDifference = 10000L
                    if (idAndTimestamp.containsKey(update.message!!.chat.id)) {
                        lastUpdateDifference = idAndTimestamp[update.message!!.chat.id]!!.until(LocalDateTime.now(), ChronoUnit.MINUTES)
                    }
                    if (idAndTimestamp.containsKey(update.message!!.chat.id) && lastUpdateDifference > 2) {
                        idAndTimestamp[update.message!!.chat.id] = LocalDateTime.now()
                        bot.sendMessage(chatId = update.message!!.chat.id, text = "Thank you ${update.message!!.from!!.firstName}!")
                    } else if (!idAndTimestamp.containsKey(update.message!!.chat.id)) {
                        idAndTimestamp[update.message!!.chat.id] = LocalDateTime.now()
                        bot.sendMessage(chatId = update.message!!.chat.id, text = "Thank you ${update.message!!.from!!.firstName}!")
                    }
                }
            }
        }
    }

    fixedRateTimer( period= TimeUnit.DAYS.toMillis(1)){
        println("Clean Up vote session")
        val tmp = mutableListOf<String>()

        val now = LocalDateTime.now()
        for (key in voteSession.keys) {
            if((voteSession[key]!!["timestamp"] as LocalDateTime).until(now,ChronoUnit.DAYS)>=1) {
                tmp.add(key)
            }
        }

        for (toremove in tmp) {
            bot.editMessageReplyMarkup(chatId = voteSession[toremove]!!["chatId"]as Long, messageId = voteSession[toremove]!!["firstMessageId"]as Long, replyMarkup = InlineKeyboardMarkup(listOf(listOf())))
            bot.editMessageReplyMarkup(chatId = voteSession[toremove]!!["chatId"]as Long, messageId = voteSession[toremove]!!["secondMessageId"]as Long, replyMarkup = InlineKeyboardMarkup(listOf(listOf())))
            voteSession.remove(toremove)
        }
    }

    bot.startPolling()

}

private fun ShowRandomRequest(index: Int, collection: DatabaseReference, bot: Bot, chatId: Long) {
    if (index != 0) {
        val startIndex = (Math.random() * index).toInt()

        val query = collection
                .orderByChild("index")
                .equalTo(startIndex.toDouble())

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val data = dataSnapshot.value as HashMap<String, Any>
                    val fileID = data.keys.first()
                    val messageId = bot.sendPhoto(chatId = chatId, photo = fileID).first!!.body()!!.result!!.messageId
                    bot.editMessageReplyMarkup(chatId = chatId, messageId = messageId, replyMarkup = inlineKeyboardMainMenu)
                } catch (e: Exception) {

                }
            }

            override fun onCancelled(dataError: DatabaseError) {

            }
        })
    }
}

private fun HighscoreRequest(collection: DatabaseReference, bot: Bot, chatId: Long) {
    val query = collection
            .orderByChild("score")
            .limitToLast(3)

    query.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            try {
                val keys = dataSnapshot.children.toList().reversed() as List<DataSnapshot>
                bot.sendPhoto(chatId = chatId, caption = "First Place \uD83E\uDD47 - scores:${(keys[0].value as HashMap<String,Any>)["score"]}", photo = keys[0].key)
                bot.sendPhoto(chatId = chatId, caption = "Second Place \uD83E\uDD48 - scores:${(keys[1].value as HashMap<String,Any>)["score"]}", photo = keys[1].key)
                val messageId=bot.sendPhoto(chatId = chatId, caption = "Third Place \uD83E\uDD49 - scores:${(keys[2].value as HashMap<String,Any>)["score"]}", photo = keys[2].key).first!!.body()!!.result!!.messageId
                bot.editMessageReplyMarkup(chatId = chatId, messageId = messageId, replyMarkup = inlineKeyboardMainMenu)
            } catch (e: Exception) {

            }
        }

        override fun onCancelled(dataError: DatabaseError) {

        }
    })
}

private fun VoteRequest(index: Int, collection: DatabaseReference, bot: Bot, chatId: Long, voteSessionMessageId: MutableMap<String, HashMap<String, Any>>) {
    val randomId = UUID.randomUUID()

    val split=randomId.toString().split("-")
    val uuid = split[1]+split[3].subSequence(0,2)

    val firstPhoto = (Math.random() * index).toInt()
    var secondPhoto = firstPhoto

    while (secondPhoto == firstPhoto) {

        secondPhoto = (Math.random() * index).toInt()
    }

    val query = collection
            .orderByChild("index")
            .equalTo(firstPhoto.toDouble())

    query.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            try {
                val data = dataSnapshot.value as HashMap<String, HashMap<String, Any>>
                val values= data.values.first()
                val fileID = values["file_id"] as String
                val inlineKeyboardfirst = InlineKeyboardMarkup(listOf(listOf(
                        InlineKeyboardButton("Vote Me", callbackData = "$uuid:$fileID")
                )))

                val messageIdfirst = bot.sendPhoto(chatId = chatId, photo = fileID,caption = "by ${values["by"]}").first!!.body()!!.result!!.messageId
                bot.editMessageReplyMarkup(chatId = chatId, messageId = messageIdfirst, replyMarkup = inlineKeyboardfirst)

                val query = collection
                        .orderByChild("index")
                        .equalTo(secondPhoto.toDouble())

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        try {
                            val data = dataSnapshot.value as HashMap<String, HashMap<String, Any>>
                            val values= data.values.first()
                            val fileID = values["file_id"] as String
                            val inlineKeyboardsecond = InlineKeyboardMarkup(listOf(listOf(
                                    InlineKeyboardButton("Vote Me", callbackData = "$uuid:$fileID")
                            )))
                            val messageIdsecond = bot.sendPhoto(chatId = chatId, photo = fileID,caption = "by ${values["by"]}").first!!.body()!!.result!!.messageId
                            bot.editMessageReplyMarkup(chatId = chatId, messageId = messageIdsecond, replyMarkup = inlineKeyboardsecond)
                            voteSessionMessageId[uuid] = hashMapOf(
                                    "firstMessageId" to messageIdfirst,
                                    "secondMessageId" to messageIdsecond,
                                    "timestamp" to LocalDateTime.now(),
                                    "chatId" to chatId
                            )
                        } catch (e: Exception) {

                        }
                    }

                    override fun onCancelled(dataError: DatabaseError) {

                    }
                })
            } catch (e: Exception) {

            }
        }

        override fun onCancelled(dataError: DatabaseError) {

        }
    })
}