package org.linkmessenger.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.linkmessenger.ProfileJavaPresenter
import org.linkmessenger.billing.viewmodel.BuyPremiumViewModel
import org.linkmessenger.collections.viewmodels.CollectionsViewModel
import org.linkmessenger.contacts.repository.ContactsRepository
import org.linkmessenger.contacts.repository.ContactsRepositoryImpl
import org.linkmessenger.contacts.viewmodel.ContactsViewModel
import org.linkmessenger.fire.viewmodel.FireActivityViewModel
import org.linkmessenger.home.viewmodel.HomeViewModel
import org.linkmessenger.notifications.viewmodels.NotificationsViewModel
import org.linkmessenger.posts.repository.PostRepository
import org.linkmessenger.posts.repository.PostRepositoryImpl
import org.linkmessenger.posts.repository.VideoRepository
import org.linkmessenger.posts.repository.VideoRepositoryImpl
import org.linkmessenger.posts.viewmodel.PostLikesViewModel
import org.linkmessenger.posts.viewmodel.PostsViewModel
import org.linkmessenger.posts.viewmodel.SinglePostViewModel
import org.linkmessenger.profile.repository.ProfileRepository
import org.linkmessenger.profile.repository.ProfileRepositoryImpl
import org.linkmessenger.profile.viewmodel.FollowersAndFollowingViewModel
import org.linkmessenger.profile.viewmodel.MyProfileViewModel
import org.linkmessenger.profile.viewmodel.PostsViewerViewModel
import org.linkmessenger.profile.viewmodel.ProfileViewModel
import org.linkmessenger.profile.viewmodel.ShareProfileViewModel
import org.linkmessenger.reports.repository.ReportRepository
import org.linkmessenger.reports.repository.ReportRepositoryImpl
import org.linkmessenger.reports.viewmodels.ReportsViewModel
import org.linkmessenger.stickers.repository.StickersRepository
import org.linkmessenger.stickers.repository.StickersRepositoryImpl
import org.linkmessenger.stickers.viewmodels.StickersByCategoryViewModel
import org.linkmessenger.stickers.viewmodels.StickersViewModel
import org.linkmessenger.trending.viewmodels.TrendingViewModel

val appModule = module {
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get(), get(), get(named("analyticsPrefs"))) }
    single<PostRepository> { PostRepositoryImpl(get(), get(), get()) }
    single<VideoRepository> { VideoRepositoryImpl(get(), get()) }
    single<ReportRepository> { ReportRepositoryImpl(get()) }
    single<ContactsRepository> { ContactsRepositoryImpl(get()) }
    single<StickersRepository> { StickersRepositoryImpl(get()) }
    single(named("analyticsPrefs")) { provideAnalyticsPreferences(androidApplication()) }
    single { PostsViewModel(get(), get(), get(named("analyticsPrefs"))) }
    single { MyProfileViewModel(get(), get()) }
    factory { FireActivityViewModel(get()) }
    factory { ShareProfileViewModel() }
    single { HomeViewModel(get(), get(named("analyticsPrefs"))) }
    viewModel { CollectionsViewModel(get()) }
    viewModel { FollowersAndFollowingViewModel(get()) }
    viewModel { SinglePostViewModel(get()) }
    factory { ProfileJavaPresenter(get()) }
    single { ContactsViewModel(get(), get()) }
    viewModel { ReportsViewModel(get()) }
    viewModel { NotificationsViewModel(get ()) }
    viewModel { PostLikesViewModel(get()) }
    viewModel{ TrendingViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { BuyPremiumViewModel() }
    viewModel { PostsViewerViewModel(get(), get()) }
    viewModel{StickersViewModel(get())}
    viewModel{StickersByCategoryViewModel(get())}
}
private fun provideAnalyticsPreferences(app: Application): SharedPreferences =
    app.getSharedPreferences("analytics", Context.MODE_PRIVATE)