package com.wzt.media.camera

import android.app.Activity
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.TextureView.SurfaceTextureListener
import com.wzt.media.camera.camera2.Camera2Manager
import com.wzt.media.databinding.ActivityCamera2Binding


class Camera2Activity:Activity() {

    private lateinit var binding: ActivityCamera2Binding
    private val manager = Camera2Manager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamera2Binding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    override fun onResume() {
        super.onResume()
        binding.textureView.surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                manager.setSurfaceTexture(surface)
                binding.textureView1.surfaceTextureListener = object : SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        manager.setSurfaceTexture1(surface)
                        manager.startPreview()
                    }

                    override fun onSurfaceTextureSizeChanged(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                    }

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                        return false
                    }

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                }
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.stopPreview()
    }


}