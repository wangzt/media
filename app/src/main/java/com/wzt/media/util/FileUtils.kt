package com.wzt.media.util

import android.content.Context
import java.io.File
import java.io.IOException

object FileUtils {

    private var BASE_ROOT_PATH: String? = null
    private val ROOT_DIR_NAME = "media"

    fun getRootPath(): String? {
        if (BASE_ROOT_PATH == null) {
            initRootPath()
        }
        return BASE_ROOT_PATH
    }

    private fun initRootPath() {
        val context: Context = AppEnv.getContext()
        if (BASE_ROOT_PATH == null) {
            var file: File? = null
            try {
                file = context.getExternalFilesDir(ROOT_DIR_NAME)
                file!!.mkdirs()
                if (file.exists() && file.canRead() && file.canWrite()) {
                    //如果可读写，则使用此目录
                    val path = file.absolutePath
                    if (path.endsWith("/")) {
                        BASE_ROOT_PATH = file.absolutePath
                    } else {
                        BASE_ROOT_PATH = file.absolutePath + "/"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (BASE_ROOT_PATH == null) {
                //如果走到这里，说明外置sd卡不可用
                if (context != null) {
                    file = context.filesDir
                    val path = file.absolutePath
                    if (path.endsWith("/")) {
                        BASE_ROOT_PATH =
                            file.absolutePath + ROOT_DIR_NAME.toString() + "/"
                    } else {
                        BASE_ROOT_PATH =
                            file.absolutePath + "/" + ROOT_DIR_NAME + "/"
                    }
                } else {
                    BASE_ROOT_PATH =
                        "/sdcard/" + ROOT_DIR_NAME.toString() + "/"
                }
            }
        }
        val file: File = File(BASE_ROOT_PATH)
        if (!file.exists()) {
            file.mkdirs()
        } else if (!file.isDirectory) {
            deleteFile(file)
            file.mkdirs()
        }
    }

    /**
     * 删除文件或者目录
     *
     * @param file 要删除的文件
     */
    fun deleteFile(file: File?) {
        if (file != null && file.exists()) {
            if (file.isDirectory) {
                val filelist = file.listFiles()
                if (filelist != null && filelist.size > 0) {
//                    File[] delFiles = file.listFiles();
                    for (delFile in filelist) {
                        if (delFile.exists()) deleteFile(delFile)
                    }
                }
            }
            file.delete()
        }
    }

    /**
     * 删除目录内容
     *
     * @param file 要删除的目录
     */
    fun clearFolder(file: File?) {
        if (file != null && file.exists()) {
            if (file.isDirectory) {
                val filelist = file.listFiles()
                if (filelist != null && filelist.size > 0) {
//                    File[] delFiles = file.listFiles();
                    for (delFile in filelist) {
                        if (delFile.exists()) deleteFile(delFile)
                    }
                }
            }
        }
    }

    /**
     * 创建目录
     *
     * @param dir
     */
    fun createDir(dir: String?) {
        val f = File(dir)
        if (!f.exists()) {
            f.mkdirs()
        }
    }

    /**
     * 创建文件
     *
     * @param file
     */
    fun createFile(file: String?) {
        val f = File(file)
        if (f != null && !f.exists()) {
            try {
                f.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 创建nomedia文件，屏蔽媒体文件
     *
     * @param path
     */
    fun createNoMediaFile(path: String) {
        val file = path + File.separator + ".nomedia"
        createFile(file)
    }

    /**
     * 删除nomedia
     *
     * @param path
     */
    fun deleteNoMediaFile(path: String) {
        val filePath = path + File.separator + ".nomedia"
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }

    fun getCaptureFilePath(): String? {
        val folderPath: String = getRootPath() + "capture"
        val f = File(folderPath)
        if (f != null && !f.exists()) {
            f.mkdirs()
        }
        createNoMediaFile(folderPath)
        return folderPath + File.separator + System.currentTimeMillis() + ".jpg"
    }
}