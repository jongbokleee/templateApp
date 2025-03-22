package com.bienbetter.application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bienbetter.application.adapter.HospitalAdapter
import com.bienbetter.application.databinding.FragmentHospitalSearchBinding
import com.bienbetter.application.model.Hospital
import com.google.firebase.database.*

class HospitalSearchFragment : Fragment() {

    private lateinit var binding: FragmentHospitalSearchBinding
    private lateinit var hospitalAdapter: HospitalAdapter
    private var hospitalList = mutableListOf<Hospital>()
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHospitalSearchBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference.child("hospitals")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hospitalAdapter = HospitalAdapter(hospitalList)
        binding.recyclerViewHospitals.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHospitals.adapter = hospitalAdapter

        val spinnerItems = resources.getStringArray(R.array.search_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSearchType.adapter = adapter

        fetchHospitalsFromFirebase("") // 기본 전체 병원 목록 조회

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            val searchType = binding.spinnerSearchType.selectedItem.toString()

            if (query.isEmpty()) {
                fetchHospitalsFromFirebase("")
            } else {
                fetchHospitalsFromFirebase(query, isSearchByName = (searchType == "병원명"))
            }
        }
    }

    private fun fetchHospitalsFromFirebase(searchText: String, isSearchByName: Boolean = true) {
        hospitalList.clear()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (citySnapshot in snapshot.children) {
                    for (districtSnapshot in citySnapshot.children) {
                        for (hospitalSnapshot in districtSnapshot.children) {
                            val name = hospitalSnapshot.child("name").getValue(String::class.java) ?: continue
                            val address = hospitalSnapshot.child("address").getValue(String::class.java) ?: ""
                            val phone = hospitalSnapshot.child("phone").getValue(String::class.java) ?: ""

                            val isMatch = if (isSearchByName) {
                                name.contains(searchText, ignoreCase = true)
                            } else {
                                address.contains(searchText, ignoreCase = true)
                            }

                            if (searchText.isEmpty() || isMatch) {
                                hospitalList.add(Hospital(name, address, phone))
                            }
                        }
                    }
                }

                if (hospitalList.isNotEmpty()) {
                    hospitalAdapter.updateList(hospitalList)
                    binding.recyclerViewHospitals.visibility = View.VISIBLE
                    binding.tvNoResults.visibility = View.GONE
                } else {
                    hospitalAdapter.updateList(emptyList())
                    binding.recyclerViewHospitals.visibility = View.GONE
                    binding.tvNoResults.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE_ERROR", "Failed to load hospitals: ${error.message}")
                binding.recyclerViewHospitals.visibility = View.GONE
                binding.tvNoResults.visibility = View.VISIBLE
            }
        })
    }
}
