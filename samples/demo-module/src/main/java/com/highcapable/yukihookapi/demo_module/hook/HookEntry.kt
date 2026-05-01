package me.voltual.lslockrotator.hook

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed(isUsingResourcesHook = false)
object HookEntry : IYukiHookXposedInit {

    override fun onInit() = YukiHookAPI.configs {
        isDebug = true // 开启调试日志，方便在 LSPosed 日志中查看是否生效
    }

    override fun onHook() = YukiHookAPI.encase {
        
        // 目标包名 SystemUI
        loadApp(name = "com.android.systemui") {
            
            var isIconHooked = false

            // 定义一个通用的旋转逻辑任务
            val rotateLockIconTask = { view: View ->
                if (!isIconHooked) {
                    isIconHooked = true
                    
                    view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) {
                            // 延迟 1000ms 等待 SystemUI 内部视图初始化完毕 (参考 Iconify 的做法)
                            Handler(Looper.getMainLooper()).postDelayed({
                                val rootView = v.parent as? ViewGroup ?: return@postDelayed
                                val context = v.context

                                // 获取 keyguard_root_view 的 ID
                                val rootId = context.resources.getIdentifier("keyguard_root_view", "id", context.packageName)
                                if (rootView.id != rootId) return@postDelayed

                                // 获取背景和前景图标的 ID
                                val bgId = context.resources.getIdentifier("device_entry_icon_bg", "id", context.packageName)
                                val fgId = context.resources.getIdentifier("device_entry_icon_fg", "id", context.packageName)

                                // 执行旋转 180 度
                                if (bgId != 0) {
                                    rootView.findViewById<View>(bgId)?.apply {
                                        rotation = 180f
                                    }
                                }
                                if (fgId != 0) {
                                    rootView.findViewById<View>(fgId)?.apply {
                                        rotation = 180f
                                    }
                                }
                                
                            }, 1000)
                        }

                        override fun onViewDetachedFromWindow(v: View) {}
                    })
                }
            }

            // Hook AodBurnInLayer (通常存在于原生或类原生系统中)
            "com.android.systemui.keyguard.ui.view.layout.sections.AodBurnInLayer".toClassOrNull()?.apply {
                constructor().hookAll {
                    after {
                        rotateLockIconTask(instance<View>())
                    }
                }
            }

            // Hook KeyguardStatusView (作为备用方案，兼容 Android 16 或部分修改过的 ROM)
            "com.android.keyguard.KeyguardStatusView".toClassOrNull()?.apply {
                constructor().hookAll {
                    after {
                        rotateLockIconTask(instance<View>())
                    }
                }
            }
        }
    }
}