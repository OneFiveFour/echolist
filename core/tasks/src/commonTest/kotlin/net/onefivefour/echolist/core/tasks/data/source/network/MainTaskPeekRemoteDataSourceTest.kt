package net.onefivefour.echolist.core.tasks.data.source.network

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import net.onefivefour.echolist.core.networking.data.error.NetworkException
import tasks.v1.ListMainTaskPeekReqeust
import tasks.v1.ListMainTaskPeekResponse
import tasks.v1.MainTaskPeek

class MainTasksSummaryRemoteDataSourceTest : FunSpec({
    test("uses the peek RPC path and real Wire request and response adapters") {
        val response = ListMainTasksSummaryResponse(
            tasks = listOf(
                MainTasksSummary(
                    main_task_id = "task-1",
                    description = "Milch",
                    is_done = false,
                    due_date = "2026-10-22",
                    task_list_id = "list-1",
                    task_list_name = "Einkauf"
                )
            )
        )
        val client = RecordingPeekClient(Result.success(ListMainTasksSummaryResponse.ADAPTER.encode(response)))
        val source = TaskListRemoteDataSourceImpl(client)

        source.listMainTasksSummary(ListMainTasksSummaryRequest(parent_dir = "projects")) shouldBe response
        client.path shouldBe "/tasks.v1.TaskListService/ListMainTasksSummary"
        ListMainTasksSummaryRequest.ADAPTER.decode(requireNotNull(client.requestBytes)).parent_dir shouldBe "projects"
        client.calls shouldBe 1
    }

    test("decodes an empty protobuf response as an empty response") {
        val source = TaskListRemoteDataSourceImpl(RecordingPeekClient(Result.success(byteArrayOf())))

        source.listMainTasksSummary(ListMainTasksSummaryRequest(parent_dir = "")) shouldBe ListMainTasksSummaryResponse()
    }

    test("propagates client failures") {
        val failure = NetworkException.ClientError(401, "Unauthorized")
        val source = TaskListRemoteDataSourceImpl(RecordingPeekClient(Result.failure(failure)))

        shouldThrow<NetworkException.ClientError> {
            source.listMainTasksSummary(ListMainTasksSummaryRequest(parent_dir = ""))
        } shouldBe failure
    }
})

private class RecordingPeekClient(private val response: Result<ByteArray>) : ConnectRpcClient {
    var path: String? = null
    var requestBytes: ByteArray? = null
    var calls = 0

    override suspend fun <Req, Res> call(
        path: String,
        request: Req,
        requestSerializer: (Req) -> ByteArray,
        responseDeserializer: (ByteArray) -> Res
    ): Result<Res> {
        this.path = path
        requestBytes = requestSerializer(request)
        calls++
        return response.map { responseDeserializer(it) }
    }
}