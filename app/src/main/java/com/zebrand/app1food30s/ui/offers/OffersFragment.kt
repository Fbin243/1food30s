package com.zebrand.app1food30s.ui.offers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.adapter.OfferAdapter
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.databinding.FragmentOffersBinding

class OffersFragment : Fragment() {
    private lateinit var binding: FragmentOffersBinding
    private lateinit var rcv: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOffersBinding.inflate(inflater)
        rcv = binding.offerRcv
        rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rcv.adapter = OfferAdapter(getListOffers())

        return binding.root
    }

    private fun getListOffers(): List<Offer> {
        var list = listOf<Offer>()
        return list
    }
}