package pers.chenan.code.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager

val PsiFile.pathByProject: String
    get() {
        return this.project.basePath?.let {
            this.virtualFile.path.replace(it, "")
        } ?: this.virtualFile.path
    }

fun PsiManager.findFileByUrl(url: String): PsiFile? {
    return VirtualFileManager.getInstance().findFileByUrl(url)?.let { virtualFile ->
        findFile(virtualFile)
    }
}

val PsiFile.document: Document?
    get() {
        return PsiDocumentManager.getInstance(project).getDocument(this)
    }