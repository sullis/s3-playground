# s3-playground

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)

A comprehensive testing playground and resource collection for Amazon S3 and S3-compatible object storage services.

## Overview

This project provides a testing framework for experimenting with various S3 implementations and storage providers. It includes:

- **Test suites** for multiple S3-compatible object storage providers (AWS S3, LocalStack, MinIO, Ceph, Google Cloud Storage, Cloudflare R2, Wasabi, Tigris)
- **Reusable test kits** (`S3SyncTestKit`, `S3AsyncTestKit`) for testing S3 operations
- **Comprehensive examples** demonstrating AWS SDK for Java v2 features
- **Curated resources** including videos, articles, presentations, and documentation about S3 and object storage

## Table of Contents

- [Prerequisites](#prerequisites)
- [Build and Test](#build-and-test)
- [Project Structure](#project-structure)
- [Supported Object Storage Providers](#supported-object-storage-providers)
- [Learning Resources](#learning-resources)
  - [How S3 is Built](#how-s3-is-built)
  - [AWS re:Invent Sessions](#s3-at-aws-reinvent-2024)
  - [Videos](#video)
  - [Articles](#articles)
  - [Academic Papers](#academic-papers)
- [Contributing](#contributing)
- [License](#license)

## Prerequisites

- Java 21 or later
- Maven 3.6+ (or use included Maven wrapper)
- Docker (required for running testcontainers-based tests)

## Build and Test

```bash
# Build the project
mvn clean package

# Run all tests
mvn clean test

# Run tests for a specific provider
mvn test -Dtest=S3MinioTest
mvn test -Dtest=S3LocalstackTest
```

## Project Structure

```
src/test/java/io/github/sullis/s3/playground/
├── AbstractS3Test.java          # Base test class with common functionality
├── S3AwsTest.java               # Tests for AWS S3
├── S3LocalstackTest.java        # Tests for LocalStack
├── S3MinioTest.java             # Tests for MinIO
├── S3CephTest.java              # Tests for Ceph
├── S3GoogleCloudTest.java       # Tests for Google Cloud Storage
├── S3CloudflareTest.java        # Tests for Cloudflare R2
├── S3WasabiTest.java            # Tests for Wasabi
├── S3TigrisTest.java            # Tests for Tigris Data
└── testkit/
    ├── S3TestKit.java           # Common test operations interface
    ├── S3SyncTestKit.java       # Synchronous S3 operations
    └── S3AsyncTestKit.java      # Asynchronous S3 operations
```

## Supported Object Storage Providers

This project includes tests for the following S3-compatible storage providers:

| Provider | Test Class | Container/Setup |
|----------|-----------|-----------------|
| AWS S3 | `S3AwsTest` | Real AWS credentials required |
| LocalStack | `S3LocalstackTest` | Testcontainers |
| MinIO | `S3MinioTest` | Testcontainers |
| Ceph | `S3CephTest` | Testcontainers |
| Google Cloud Storage | `S3GoogleCloudTest` | Real GCS credentials required |
| Cloudflare R2 | `S3CloudflareTest` | Real R2 credentials required |
| Wasabi | `S3WasabiTest` | Real Wasabi credentials required |
| Tigris Data | `S3TigrisTest` | Real Tigris credentials required |
| Adobe S3Mock | `S3MockTest` | Testcontainers |

## Learning Resources

Below is a curated collection of resources about Amazon S3 and object storage systems.

### How S3 is Built
- [how s3 is built](https://www.youtube.com/watch?v=5vL6aCvgQXU) -- January 2026 - Pragmatic Engineer

### S3 at AWS re:Invent 2025
- [AWS re:Invent 2025 - What's new with Amazon S3](https://www.youtube.com/watch?v=Sy2LHRyMXAo) (STG206)
- [AWS re:Invent 2025 - Deep dive on Amazon S3](https://www.youtube.com/watch?v=S4swTRi1i0w) (STG407)
- [AWS re:Invent 2025 - Amazon S3 Tables architecture, use cases, and best practices](https://www.youtube.com/watch?v=Pi82g0YGklU) (STG334)
- [AWS re:Invent 2025 - Transforming AI storage economics with Amazon S3 Vectors](https://www.youtube.com/watch?v=ghUW2SpEYPk) (STG318)
- [AWS re:Invent 2025 - Amazon S3 security and access control best practices](https://www.youtube.com/watch?v=UfGW7RoaNhc) (STG316)
- [AWS re:Invent 2025 - How Netflix uses Amazon S3 Storage Lens to track exabytes of data](https://www.youtube.com/watch?v=Q2YoHfhFuI8) (STG214)
- [AWS re:Invent 2025 - Building multi-Region data lakes with Replication for Amazon S3 Tables](https://www.youtube.com/watch?v=3aca3axKgGs) (STG358)

### S3 at AWS re:Invent 2024
- [AWS re:invent 2024 - What's new with Amazon S3](https://www.youtube.com/watch?v=pbsIVmWqr2M)
- [AWS re:Invent 2024 - Dive deep on Amazon S3](https://www.youtube.com/watch?v=NXehLy7IiPM)
- [AWS re:Invent 2024 - Store tabular data at scale with Amazon S3 Tables](https://www.youtube.com/watch?v=1U7yX4HTLCI)
- [AWS re:Invent 2024 - Scaling and evolving media storage at Netflix with Amazon S3](https://www.youtube.com/watch?v=Nd9ebOzucj4)
- [AWS re:Invent 2024 - Amazon S3 security and access control best practices](https://www.youtube.com/watch?v=vRmUI0VdsQw)
- [AWS re:Invent 2024 - Unlock the power of your data with Amazon S3 Metadata](https://www.youtube.com/watch?v=hB0AxWKh4wA)
- [AWS re:Invent 2024 - Build and optimize a data lake on Amazon S3](https://www.youtube.com/watch?v=SIGpBvmlick)
- [AWS re:Invent 2024 - Beyond 11 9s of durability: Data protection with Amazon S3](https://www.youtube.com/watch?v=XyRdMT4zUrA)
- [AWS re:Invent 2024 - Efficient incremental processing with Apache Iceberg at Netflix](https://www.youtube.com/watch?v=s1ySnxVg5rk)

### S3 at AWS re:Invent 2023

- [AWS re:Invent 2023 - Solving large-scale data access challenges with Amazon S3](https://www.youtube.com/watch?v=Ts-ZMBzGeh0)
- [AWS re:Invent 2023 - Optimizing storage price and performance with Amazon S3](https://www.youtube.com/watch?v=RxgYNrXPOLw)
- [AWS re:Invent 2023 - What’s new with Amazon S3](https://www.youtube.com/watch?v=idz2SvBHK-s)
- [AWS re:Invent 2023 - Dive deep on Amazon S3](https://www.youtube.com/watch?v=sYDJYqvNeXU)
- [AWS re:Invent 2023 - Building and optimizing a data lake on Amazon S3](https://www.youtube.com/watch?v=mpQa_Zm1xW8)
- [AWS re:Invent 2023 - AWS storage: The backbone for your data-driven business](https://www.youtube.com/watch?v=Alxig9GFIE4) - Andy Warfield
- [AWS re:Invent 2023 - Netflix’s journey to an Apache Iceberg–only data lake](https://www.youtube.com/watch?v=jMFMEk8jFu8)

### S3 storage classes
- [using S3 storage classes](https://docs.aws.amazon.com/AmazonS3/latest/userguide/storage-class-intro.html)

### S3 Consistency
- [Read-after-Write consistency](https://aws.amazon.com/blogs/aws/amazon-s3-update-strong-read-after-write-consistency/)

### S3 Express One Zone storage
- [Working with S3 Express One Zone](https://docs.aws.amazon.com/AmazonS3/latest/userguide/s3-express-SDKs.html)
- [Getting started with the Amazon S3 Express One Zone storage class](https://www.youtube.com/watch?v=MzZ5pZ-wXBM)
- [s3 express one zone @ Clickhouse Cloud](https://aws.amazon.com/blogs/storage/clickhouse-cloud-amazon-s3-express-one-zone-making-a-blazing-fast-analytical-database-even-faster/)
- [S3 Express One Zone, Not Quite What I Hoped For](https://jack-vanlightly.com/blog/2023/11/29/s3-express-one-zone-not-quite-what-i-hoped-for) - Jack Vanlightly
- [A Cost Analysis Of Replication Vs S3 Express One Zone In Transactional Data Systems](https://jack-vanlightly.com/blog/2024/6/10/a-cost-analysis-of-replication-vs-s3-express-one-zone-in-transactional-data-systems) - Jack Vanlightly

### S3 event notifications
- [Event Notifications](https://docs.aws.amazon.com/AmazonS3/latest/userguide/EventNotifications.html)

### S3 encryption
- [server side encryption](https://docs.aws.amazon.com/AmazonS3/latest/userguide/UsingServerSideEncryption.html)

### S3 List performance
- [Delta Lake pdf](https://people.eecs.berkeley.edu/~matei/papers/2020/vldb_delta_lake.pdf)

### Databases on Object Storage
- [DB on object storage](https://resources.min.io/october-24-newsletter-sg/databases-on-object-storage)

### SlateDb
- [SlateDB and S3](https://slatedb.io/docs/tutorials/s3/)
- [Internals of SlateDB](https://www.youtube.com/watch?v=gcTRXZeKbNg)

### Zero Disk Architecture
- [Zero Disk Architecture](https://avi.im/blag/2024/zero-disk-architecture/)
- [Zero disks is better](https://www.warpstream.com/blog/zero-disks-is-better-for-kafka) - warpstream

### Diskless Kafka
- [Ins and Outs of KIP-1150: Diskless Topics in Apache Kafka](https://www.youtube.com/watch?v=hrMvOFoQ3X4)

### RisingWave and S3
- [Towards Sub-100ms Latency Stream Processing with an S3-Based Architecture](https://risingwave.com/blog/sub-100ms-stream-processing-s3-cloud-native/)
- [S3 as primary storage](https://risingwave.com/blog/how-we-built-risingwave-on-s3-a-deep-dive-into-s3-as-primary-storage-architecture/)

### Presentations
- [Amazon S3 at Chicago Java User Group](https://speakerdeck.com/sullis/amazon-s3-chicago-2025-06-04) - June 2025
- [Amazon S3 at Boston Lakehouse meetup](https://speakerdeck.com/sullis/amazon-s3-boston-2025-05-07) - May 2025
- [Amazon S3 at NYJavaSIG](https://speakerdeck.com/sullis/amazon-s3-nyjavasig-2024-12-12) - December 2024
- [The Rise of Object Store Architectures](https://www.linkedin.com/posts/tednaleid_the-rise-of-object-store-architectures-activity-7310822744076472322-uay-?utm_source=social_share_send&utm_medium=member_desktop_web&rcm=ACoAAABsl_wBt-O6pdpGoZIeFdwL4P2DQYZ_JqU)
  
### Video
- [Object Storage is all you need](https://www.youtube.com/watch?v=ei0wwTy6_G4) - Justin Cormack, KubeCon November 2024
- [Building and Operating a Pretty Big Storage System (My Adventures in Amazon S3)](https://www.usenix.org/conference/fast23/presentation/warfield) - Andy Warfield @ FAST '23
- [Reimplementing Apache Kafka with Golang and S3](https://www.youtube.com/watch?v=xgzmxe6cj6A) - Ryan Worl, WarpStream
- [Beyond Kafka: Cutting Costs and Complexity with WarpStream and S3](https://www.youtube.com/watch?v=wgwUE2izH38) - Ryan Worl, WarpStream
- [Beyond Tiered Storage: Deep dive into WarpStream's Storage Engine](https://www.youtube.com/watch?v=74ZuGhNP3w8) - Richie Artoul, WarpStream @ Current 2024
- [Best Practices for Trino with Amazon S3](https://www.youtube.com/watch?v=cjUUcHlUKxQ) ([slides](https://trino.io/assets/blog/trino-fest-2024/aws-s3.pdf)) - Trino Fest 2024
- [Mastering S3 permissions](https://www.youtube.com/watch?v=6oYJTyggbfM) - AWS
- [Improving your Amazon S3 security with cost-effective practices ](https://www.youtube.com/watch?v=FA1kLC4dHvA) - AWS Re:Inforce 2024
- [SNIA: Navigating Complexities of Object Storage Compatibility](https://www.youtube.com/watch?v=o6RIPL-S8sA)
- [Minio for developers](https://www.youtube.com/watch?v=gY090GEDdu8)

### Open source projects
- [s3proxy](https://github.com/gaul/s3proxy)
- [S3Mock](https://github.com/adobe/S3Mock)
- [testcontainers-ceph](https://github.com/jarlah/testcontainers-ceph)
- [Active Storage](https://guides.rubyonrails.org/v6.0.3/active_storage_overview.html) - Ruby on Rails
- [s3-tests](https://github.com/ceph/s3-tests)
- [AWS crt s3 benchmarks](https://github.com/awslabs/aws-crt-s3-benchmarks)
- [Apache Pinot: s3 object storage](https://docs.pinot.apache.org/users/tutorials/use-s3-as-deep-store-for-pinot)
- [LinkedIn Ambry](https://github.com/linkedin/ambry)
- [s3fs-fuse](https://github.com/s3fs-fuse/s3fs-fuse)
- [slatedb](https://github.com/slatedb/slatedb)

### Benchmarks
- [warp](https://github.com/minio/warp) - s3 benchmarking tool
- [object-store-bench](https://github.com/projectnessie/object-store-bench)
- [cosbench](https://github.com/sine-io/cosbench-sineio)

### Commercial offerings
- [Wasabi S3 API](https://docs.wasabi.com/docs/wasabi-api)
- [flexify](https://flexify.io)

### BYOB (Bring your own bucket)
- [Axiom BYOB](https://axiom.co/blog/bring-your-own-bucket)
- [Bitdrift BYOB](https://blog.bitdrift.io/post/bring-your-own-bucket)

### S3 Multi Part Upload
- [multipart upload](https://docs.aws.amazon.com/AmazonS3/latest/userguide/mpu-upload-object.html) guide
- [S3 Transfer Manager](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/transfer/s3/package-summary.html)

### S3 conditional writes
- [s3 user guide: prevent object overwrites with conditional writes](https://docs.aws.amazon.com/AmazonS3/latest/userguide/conditional-writes.html)
- [conditional write with ETag](https://aws.amazon.com/about-aws/whats-new/2024/11/amazon-s3-functionality-conditional-writes/)
- [S3 now supports conditional writes](https://aws.amazon.com/about-aws/whats-new/2024/08/amazon-s3-conditional-writes/)
- [What’s the Big Deal with Conditional Writes Support in S3?](https://www.tigrisdata.com/blog/s3-conditional-writes/)
- [Leader election with conditional writes](https://www.morling.dev/blog/leader-election-with-s3-conditional-writes/) - Gunnar Morling
- [Implementing leader election with Google Cloud Storage](https://cloud.google.com/blog/topics/developers-practitioners/implementing-leader-election-google-cloud-storage)
- [Delta Lake: conditional writes](https://github.com/delta-io/delta/issues/3596)
- [Localstack s3 conditional writes](https://github.com/localstack/localstack/pull/11402)
- [Minio conditional writes](https://resources.min.io/c/leading-the-way-minios-conditional-write-feature?x=Fg6JFL)
- [conditional writes in arrow-rs](https://github.com/apache/arrow-rs/pull/6682)

### Java AWS SDK
- [software.amazon.awssdk.services.s3.S3Uri](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/s3/S3Uri.html)
- [S3 URI parsing](https://aws.amazon.com/blogs/devops/s3-uri-parsing-is-now-available-in-aws-sdk-for-java-2-x/)

### Minio alternatives
- [minio alternatives](https://www.infoq.com/news/2025/12/minio-s3-api-alternatives/)

### Articles
- [Why We Built Another Object Storage](https://fractalbits.com/blog/why-we-built-another-object-storage/)
- [Garbage collection of Object Storage](https://www.warpstream.com/blog/taking-out-the-trash-garbage-collection-of-object-storage-at-massive-scale)
- [CORS configs on Amazon S3](https://aws.amazon.com/blogs/media/deep-dive-into-cors-configs-on-aws-s3/)
- [Around the world in 15 buckets](https://cloudiamo.com/2024/12/15/around-the-world-in-15-buckets/)
- [Delta Lake and S3](https://delta.io/blog/delta-lake-s3/)
- [Registering S3 files into Apache Iceberg tables- without the rewrites](https://medium.com/inquery-data/registering-s3-files-into-apache-iceberg-tables-without-the-rewrites-3c087cb01658)
- [Materialize: Bulk exports to S3](https://materialize.com/blog/bulk-exports-s3/)
- [How Fetch reduced latency on image uploads using Amazon S3 Express One Zone](https://aws.amazon.com/blogs/storage/how-fetch-reduces-latency-on-image-uploads-using-amazon-s3-express-one-zone/)
- [Building and operating a pretty big storage system called S3](https://www.allthingsdistributed.com/2023/07/building-and-operating-a-pretty-big-storage-system.html) - Andy Warfield
- [Malware protection for S3](https://aws.amazon.com/blogs/aws/introducing-amazon-guardduty-malware-protection-for-amazon-s3/)
- [Cost Analysis Of Replication Vs S3 Express One Zone](https://jack-vanlightly.com/blog/2024/6/10/a-cost-analysis-of-replication-vs-s3-express-one-zone-in-transactional-data-systems)
- [S3 is showing its age](https://materializedview.io/p/s3-is-showing-its-age)
- [Minimizing S3 API Costs with Distributed mmap](https://www.warpstream.com/blog/minimizing-s3-api-costs-with-distributed-mmap)
- [How WarpStream enables cost-effective low-latency streaming with Amazon S3 Express One Zone](https://aws.amazon.com/blogs/storage/how-warpstream-enables-cost-effective-low-latency-streaming-with-amazon-s3-express-one-zone/)
- [S3 durability](https://newsletter.systemdesign.one/p/amazon-s3-durability)

### S3 Migrations
- [Dropbox migration to S3](https://www.youtube.com/watch?v=6x-XGJQwk2M)

### Small Files Problem
- [The Challenge in Big Data is Small Files](https://blog.min.io/challenge-big-data-small-files/)

### KubeCon 2024
- [Object Storage Is All You Need](https://www.youtube.com/watch?v=ei0wwTy6_G4) - Justin Cormack, Docker
- [Object, Block, or File Storage](https://www.youtube.com/watch?v=QQgmaHqKRyY) -  Mitch Becker & Tom McDonald, Amazon Web Services (AWS)

### Tigris Data object storage
- [Append Only object storage](https://www.tigrisdata.com/blog/append-only-storage/)
- [shadow buckets](https://www.tigrisdata.com/blog/shadow-bucket/)

### Academic papers
- [MIT: Building a Database on S3](https://people.csail.mit.edu/kraska/pub/sigmod08-s3.pdf) (2008)

### Gradle plugins
- [object-store-cache-plugin](https://github.com/craigatk/object-store-cache-plugin)

### Open source utilities
- [s3grep](https://github.com/dacort/s3grep)
- [s3sh](https://github.com/dacort/s3sh)

# Contributing

Contributions are welcome! This can include:

- Adding tests for new S3-compatible storage providers
- Improving test coverage and test utilities
- Adding new S3-related resources, articles, or videos
- Fixing bugs or improving documentation
- Sharing use cases and examples

Please ensure:
- Tests pass before submitting: `mvn clean test`
- Code follows existing style and conventions
- Commit messages are clear and descriptive

# License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

