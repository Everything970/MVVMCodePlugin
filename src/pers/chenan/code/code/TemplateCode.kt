package pers.chenan.code.code

object TemplateCode {
    const val packageName = "#packageName"
    const val activityName = "#activityName"
    const val lowerActivityName = "#lowerActivityName"
    const val javadoc = "#javadoc"
    const val funName = "#funName"
    const val requestParameter = "#requestParameter"
    const val resultBean = "#resultBean"
    const val interfaceURL = "#url"
    const val TYPE_VIEW_MODEL = "viewmodel"
    const val TYPE_ACTIVITY = "activity"
    const val TYPE_LAYOUT = "layout"


    const val activityCode = "package #packageName.activity\n" +
            "\n" +
            "import android.os.Bundle\n" +
            "import androidx.appcompat.app.AppCompatActivity\n" +
            "import androidx.databinding.DataBindingUtil\n" +
            "import androidx.lifecycle.ViewModelProvider\n" +
            "import #packageName.R\n" +
            "import #packageName.databinding.Activity#activityNameBinding\n" +
            "import #packageName.viewmodel.#activityNameViewModel\n" +
            "\n" +
            "class #activityNameActivity : AppCompatActivity() {\n" +
            "\n" +
            "    private lateinit var binding: Activity#activityNameBinding\n" +
            "    private lateinit var viewModel: #activityNameViewModel\n" +
            "\n" +
            "    override fun onCreate(savedInstanceState: Bundle?) {\n" +
            "        super.onCreate(savedInstanceState)\n" +
            "        binding = DataBindingUtil.setContentView(this, R.layout.activity#lowerActivityName)\n" +
            "        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)\n" +
            "            .create(#activityNameViewModel::class.java)\n" +
            "\n" +
            "\n" +
            "        binding.viewModel = viewModel\n" +
            "        lifecycle.addObserver(viewModel)\n" +
            "    }\n" +
            "}\n"

    const val viewModelCode = "package #packageName.viewmodel\n" +
            "\n" +
            "import android.app.Application\n" +
            "import androidx.lifecycle.*\n" +
            "\n" +
            "class #activityNameViewModel(application: Application) : AndroidViewModel(application), LifecycleObserver {\n" +
            "\n" +
            "    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)\n" +
            "    fun onActivityCreate(owner: LifecycleOwner) {\n" +
            "\n" +
            "    }\n" +
            "}"

    const val layoutCode = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<layout\n" +
            "        xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "        xmlns:tools=\"http://schemas.android.com/tools\"\n" +
            "        xmlns:app=\"http://schemas.android.com/apk/res-auto\">\n" +
            "    <data>\n" +
            "        <variable name=\"viewModel\" type=\"#packageName.viewmodel.#activityNameViewModel\"/>\n" +
            "    </data>\n" +
            "    <androidx.constraintlayout.widget.ConstraintLayout\n" +
            "            android:orientation=\"vertical\"\n" +
            "            android:layout_width=\"match_parent\"\n" +
            "            android:layout_height=\"match_parent\"\n" +
            "            android:id=\"@+id/cl_content\"\n" +
            "            tools:context=\".activity.#activityNameActivity\">\n" +
            "    \n" +
            "    </androidx.constraintlayout.widget.ConstraintLayout>\n" +
            "</layout>"

    const val interfaceFunCode = "#javadoc\n" +
            "@FormUrlEncoded\n" +
            "@POST(\"#url\")\n" +
            "fun #funName(#requestParameter):Call<BaseEntity<#resultBean>>\n"

    
}