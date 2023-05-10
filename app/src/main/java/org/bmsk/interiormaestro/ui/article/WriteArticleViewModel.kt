package org.bmsk.interiormaestro.ui.article

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// 라이프사이클에 맞게 데이터를 저장하기 위함
class WriteArticleViewModel: ViewModel() {
    private var _selectedUri = MutableLiveData<Uri?>()
    val selectedUri: LiveData<Uri?>
        get() = _selectedUri

    fun updateSelectedUri(uri: Uri?) {
        _selectedUri.value = uri
    }
}