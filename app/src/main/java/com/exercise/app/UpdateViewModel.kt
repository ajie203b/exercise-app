package com.exercise.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 更新流程的 ViewModel 包装。
 *
 * 把 [UpdateManager] 放进 ViewModel 让「检查中/下载中/已下载」这些状态在旋转屏幕、
 * 切换主题等配置变更后依然存在；旧实现把管理器挂在 Activity 上，重建时会把正在进行的
 * 下载一起取消。
 */
class UpdateViewModel(app: Application) : AndroidViewModel(app) {

    private val manager = UpdateManager(app, viewModelScope)

    val state: StateFlow<UpdateState> = manager.state
    val events: Flow<UpdateEvent> = manager.events

    fun checkForUpdate() = manager.checkForUpdate()

    fun download() = manager.download()

    fun cancelDownload() = manager.cancelDownload()

    fun install() = manager.installApk()

    fun dismiss() = manager.dismiss()

    override fun onCleared() {
        manager.release()
        super.onCleared()
    }
}
