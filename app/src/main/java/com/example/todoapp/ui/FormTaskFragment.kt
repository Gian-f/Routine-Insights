package com.example.todoapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentFormTaskBinding
import com.example.todoapp.helper.BaseFragment
import com.example.todoapp.helper.FirebaseHelper
import com.example.todoapp.helper.initToolbar
import com.example.todoapp.model.Task


class FormTaskFragment : BaseFragment() {
    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!
    private val args: FormTaskFragmentArgs by navArgs()

    private lateinit var task: Task
    private var newTask: Boolean = true
    private var statusTask: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initToolbar(binding.toolbar)
        getArgs()
    }

    private fun getArgs() {
        args.task.let {
            if(it != null) {
                task = it
                configTask()
            }
        }
    }

    private fun configTask() {
        newTask = false
        statusTask = task.status
        binding.textToolbar.text = getString(R.string.text_editing_task_form_task_fragment)
        binding.edtDescription.setText(task.description)
        setStatus()
    }

    private fun setStatus() {
        binding.radioGroup.check(
            when(task.status) {
                0 -> {
                    R.id.rbTodo
                }
                1 -> {
                    R.id.rbDoing
                }
                else -> {
                    R.id.rbDone
                }
            }
        )
    }

    private fun initListener() {
        binding.btnSave.setOnClickListener { validateData() }
        binding.radioGroup.setOnCheckedChangeListener { _, id ->
            statusTask = when (id) {
                R.id.rbTodo -> 0 //tarefas a fazer
                R.id.rbDoing -> 1 //tarefas fazendo
                else -> 2  //tarefas concluídas
            }
        }
    }


    private fun validateData() {
        val description = binding.edtDescription.text.toString().trim()

        if (description.isNotEmpty()) {

            hideKeyboard()

            binding.progressBar.isVisible = true

            if (newTask) task = Task()
            task.description = description
            task.status = statusTask

            save()
        } else {
            Toast.makeText(
                requireContext(),
                "Informe uma descrição para a tarefa",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun save() {
        FirebaseHelper.getDatabase()
            .child("task")
            .child(FirebaseHelper.getUserId() ?: "")
            .child(task.id)
            .setValue(task)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (newTask) { // nova tarefa
                        findNavController().popBackStack()
                        Toast.makeText(requireContext(), "Sua atividade foi salva com sucesso.", Toast.LENGTH_SHORT).show()
                    } else { // editando tarefa
                        binding.progressBar.isVisible = false
                        Toast.makeText(requireContext(), "Sua atividade foi editada com sucesso.", Toast.LENGTH_SHORT).show()

                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ocorreu um erro ao salvar a atividade.",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                binding.progressBar.isVisible = false
                Toast.makeText(
                    requireContext(),
                    "Ocorreu um erro ao salvar a atividade.",
                    Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}