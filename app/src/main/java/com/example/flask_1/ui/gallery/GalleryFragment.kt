// GalleryFragment.kt
package com.example.flask_1.ui.gallery

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flask_1.R
import com.example.flask_1.databinding.FragmentGalleryBinding
import com.example.flask_1.ui.login.Exam
import com.example.flask_1.ui.login.RetrofitClient
import com.example.week2test.ui.gallery.RecyclerAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerAdapter
    private val examList = arrayListOf<Exam>()
    private val filteredList = arrayListOf<Exam>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // RecyclerView 설정
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = RecyclerAdapter(filteredList, R.id.action_galleryFragment_to_solvingFragment) // 네비게이션 액션 ID를 전달합니다.
        recyclerView.adapter = adapter

        // 사용자 데이터 로드
        loadExamData()

        // 검색 기능 설정
        binding.searchButton.setOnClickListener {
            val searchText = binding.searchEditText.text.toString()
            filterList(searchText)
        }

        return root
    }

    private fun loadExamData() {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: return

        RetrofitClient.apiService.getExams(mapOf("username" to username)).enqueue(object : Callback<List<Exam>> {
            override fun onResponse(call: Call<List<Exam>>, response: Response<List<Exam>>) {
                if (response.isSuccessful) {
                    response.body()?.let { exams ->
                        examList.clear()
                        examList.addAll(exams)
                        filterList("")
                    }
                } else {
                    Toast.makeText(context, "Failed to load exam data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Exam>>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterList(searchText: String) {
        filteredList.clear()
        if (searchText.isEmpty()) {
            filteredList.addAll(examList)
        } else {
            for (exam in examList) {
                if (exam.exam_name.contains(searchText, ignoreCase = true)) {
                    filteredList.add(exam)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
