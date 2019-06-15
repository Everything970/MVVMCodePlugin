package pers.chenan.code.util

import com.intellij.psi.PsiFile

val PsiFile.pathByProject: String
    get() {
        return this.project.basePath?.let {
            this.virtualFile.path.replace(it, "")
        } ?: this.virtualFile.path
    }