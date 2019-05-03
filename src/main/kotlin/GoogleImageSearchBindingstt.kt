@file:JsModule("google-images")
@file:JsNonModule

package Bindings

import kotlin.js.Promise

@JsName("Client")
external class GoogleImageSearchhh(id: String, ed: String) {

    fun search(query: String): Promise<Array<String>>
}

