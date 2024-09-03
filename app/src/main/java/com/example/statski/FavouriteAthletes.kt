package com.example.statski

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.statski.databinding.FragmentFavouriteAthletesBinding
import com.example.statski.databinding.ItemFavAthletesLayoutBinding

import java.util.Locale


class FavouriteAthletes : Fragment() {

    private lateinit var binding : FragmentFavouriteAthletesBinding
    private lateinit var FavAthl_adapter : FavouriteAthletesAdapter
    private var FavAthlList = mutableListOf<Athlete>()
    val viewModel_instance : AthletesViewModel by activityViewModels()
    private var currentFilterText : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavouriteAthletesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel_instance
        // Setting adapter instance
        FavAthl_adapter = FavouriteAthletesAdapter(requireContext(), FavAthlList)
        binding.rvFavouriteAthletesList.adapter = FavAthl_adapter



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setting RV details
        binding.rvFavouriteAthletesList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavouriteAthletesList.setHasFixedSize(true)

        FavAthlList = viewModel_instance.athletesMap.values.toMutableList()
        FavAthlList.filter{it.isFav}





        // SearchView Configuration
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentFilterText = newText
                filterList(newText)
                return true
            }
        })
    }
    private fun filterList(query : String?){
        if(query!= null){
            var lowercase_query = query.lowercase(Locale.getDefault())
            var filteredList = FavAthlList.filter{it.name.lowercase(Locale.ROOT).contains(lowercase_query)}

            if(filteredList.isEmpty()){
                Toast.makeText(requireContext(), "No Athlete found", Toast.LENGTH_SHORT).show()
            } else{
                FavAthl_adapter.setFilteredList(filteredList)
            }
        } else FavAthl_adapter.setFilteredList(FavAthlList)
    }
}

class FavouriteAthletesAdapter(val context: Context, var favList : List<Athlete>):
        RecyclerView.Adapter<FavouriteAthletesAdapter.ViewHolder>(){
            inner class ViewHolder(val binding: ItemFavAthletesLayoutBinding): RecyclerView.ViewHolder(binding.root)



    fun setFilteredList(ListAthletes : List<Athlete>){
        this.favList = ListAthletes
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFavAthletesLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var favAthlete = favList[position]


    }
    override fun getItemCount(): Int = favList.size
}