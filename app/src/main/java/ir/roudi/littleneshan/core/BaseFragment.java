package ir.roudi.littleneshan.core;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.hilt.navigation.HiltViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import ir.roudi.littleneshan.R;

public abstract class BaseFragment<VM extends BaseViewModel> extends Fragment {

    protected VM viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViewModel();
    }

    private void setupViewModel() {
        var backStackEntry = findNavController(this)
                .getBackStackEntry(R.id.nav_main);

        var factory = HiltViewModelFactory.create(requireContext(), backStackEntry);

        viewModel = new ViewModelProvider(backStackEntry, factory)
                .get(getViewModelClass());
    }

    public abstract Class<VM> getViewModelClass();

}
