


@file:JsModule("free-google-image-search")
@file:JsNonModule
package Bindings

import kotlin.js.Promise

@JsName("default")
external class GoogleImageSearch{
    companion object{
        fun searchImage(query:String):Promise<Array<String>>
    }
}


