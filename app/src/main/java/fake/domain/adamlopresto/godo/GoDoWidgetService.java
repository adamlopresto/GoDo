package fake.domain.adamlopresto.godo;

import android.content.Intent;
import android.widget.RemoteViewsService;

import org.jetbrains.annotations.Nullable;

public class GoDoWidgetService extends RemoteViewsService {

    @Nullable
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GoDoViewsFactory(this.getApplicationContext());
    }
}
