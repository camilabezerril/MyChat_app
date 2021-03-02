package androidpro.com.mychat.cadastrologin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Usuario (val uid: String, val nome: String, val imagemPerfilUrl: String) : Parcelable {
    constructor() : this("", "", "")
}