package ir.roudi.littleneshan.core;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.hilt.navigation.HiltViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import ir.roudi.littleneshan.R;

public abstract class BaseFragment<DB extends ViewDataBinding, VM extends BaseViewModel> extends Fragment {

    protected VM viewModel;
    protected DB binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), getLayoutId(), container, false);
        return binding.getRoot();
    }

    public abstract int getLayoutId();

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observerEvents();
    }

    private void observerEvents() {
        observeErrorMessage();
        observeNavigateUpEvent();
    }

    private void observeErrorMessage() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(errorMessage -> {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            });
        });
    }

    private void observeNavigateUpEvent() {
        viewModel.getNavigateUpEvent().observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(shouldNavigateUp -> {
                if(shouldNavigateUp) {
                    findNavController(BaseFragment.this).navigateUp();
                }
            });
        });
    }

}
