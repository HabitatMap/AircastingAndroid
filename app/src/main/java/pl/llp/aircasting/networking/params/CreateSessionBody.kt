package pl.llp.aircasting.networking.params

import pl.llp.aircasting.models.Photo

//todo: not sure if i can use List/Array for storing photos, according to old map vararg might be proper thing
class CreateSessionBody(val session: String, val compression: Boolean = true) //, val photos: MutableList<Photo> = mutableListOf() todo: according to old app, we may have varying number of "photos[]" parameters next to "session" and "compression" parameters
